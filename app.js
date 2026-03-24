const express = require('express');
const path = require('path');
const { engine } = require('express-handlebars');
const cookieParser = require('cookie-parser');
const session = require('express-session');
const { sql, poolPromise } = require('./src/config/db');
const multer = require('multer');
const fs = require('fs');

const app = express();
const PORT = 3000;

// Tạo thư mục lưu ảnh
const uploadDir = path.join(__dirname, 'public/images');
if (!fs.existsSync(uploadDir)) fs.mkdirSync(uploadDir, { recursive: true });

const storage = multer.diskStorage({
    destination: function (req, file, cb) { cb(null, uploadDir); },
    filename: function (req, file, cb) {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        cb(null, 'shm-' + uniqueSuffix + path.extname(file.originalname));
    }
});
const upload = multer({ storage: storage });

const showPopup = (res, title, text, icon, redirectUrl) => {
    const action = redirectUrl === 'back' ? 'window.history.back();' : `window.location.href = '${redirectUrl}';`;
    res.send(`
        <!DOCTYPE html>
        <html><head>
            <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
            <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
            <style>body { background-color: #f8f9fa; font-family: sans-serif; }</style>
        </head><body><script>
            Swal.fire({ title: '${title}', text: '${text}', icon: '${icon}', confirmButtonColor: '#0d6efd', confirmButtonText: 'OK', allowOutsideClick: false })
            .then(() => { ${action} });
        </script></body></html>
    `);
};

const formatPrice = (price) => new Intl.NumberFormat('en-US').format(price);

app.use(session({ secret: 'shm_secret_key_2026', resave: false, saveUninitialized: true, cookie: { secure: false } }));

// Middleware kiểm tra đăng nhập
const checkLogin = (req, res, next) => {
    if (req.session.user) next();
    else showPopup(res, 'Cảnh báo!', 'Bạn cần đăng nhập để sử dụng tính năng này!', 'warning', '/login');
};

// Middleware kiểm tra quyền ADMIN
const checkAdmin = (req, res, next) => {
    if (req.session.user && req.session.user.role === 'admin') next();
    else showPopup(res, 'Cấm truy cập!', 'Bạn không có quyền vào khu vực của Admin!', 'error', '/');
};

app.use((req, res, next) => {
    res.locals.user = req.session.user || null;
    res.locals.isAdmin = req.session.user && req.session.user.role === 'admin'; // Truyền biến isAdmin ra giao diện
    if (!req.session.cart) req.session.cart = [];
    if (!req.session.recentlyViewed) req.session.recentlyViewed = [];
    res.locals.cartItemCount = req.session.cart.length; 
    next();
});

// Cấu hình Handlebars có thêm Helper để so sánh
app.engine('hbs', engine({ 
    extname: '.hbs', defaultLayout: 'main', layoutsDir: path.join(__dirname, 'views/layouts'),
    helpers: { eq: (a, b) => a === b } 
}));
app.set('view engine', 'hbs');
app.set('views', path.join(__dirname, 'views'));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

// ==========================================
// TÍNH NĂNG ADMIN DASHBOARD (LỌC + XÓA BÀI)
// ==========================================
app.get('/admin', checkAdmin, async (req, res) => {
    try {
        const pool = await poolPromise;
        const usersCount = await pool.request().query("SELECT COUNT(*) as count FROM users");
        const listingsCount = await pool.request().query("SELECT COUNT(*) as count FROM listings WHERE status = 'Active'");

        const categoriesResult = await pool.request().query("SELECT * FROM categories");
        
        const searchQuery = req.query.search || '';
        const categoryQuery = req.query.category ? parseInt(req.query.category) : '';
        const hasFilter = searchQuery !== '' || categoryQuery !== '';

        let queryListings = `
            SELECT l.listing_id AS id, l.title, l.price, u.full_name AS seller_name, l.status, c.category_name
            FROM listings l
            LEFT JOIN users u ON l.seller_id = u.user_id
            LEFT JOIN categories c ON l.category_id = c.category_id
            WHERE 1=1
        `;
        
        const request = pool.request();
        
        if (searchQuery.trim() !== '') {
            queryListings += ` AND l.title LIKE @search `;
            request.input('search', sql.NVarChar, `%${searchQuery}%`);
        }
        
        if (categoryQuery !== '') {
            queryListings += ` AND l.category_id = @category `;
            request.input('category', sql.Int, categoryQuery);
        }
        
        queryListings += ` ORDER BY l.created_at DESC `;
        
        const allListings = await request.query(queryListings);
        const formattedListings = allListings.recordset.map(item => ({ ...item, price: formatPrice(item.price) }));

        res.render('admin-dashboard', { 
            totalUsers: usersCount.recordset[0].count,
            totalListings: listingsCount.recordset[0].count,
            allListings: formattedListings,
            categories: categoriesResult.recordset,
            searchQuery: searchQuery,
            categoryQuery: categoryQuery,
            hasFilter: hasFilter
        });
    } catch (err) { res.status(500).send("Lỗi tải trang Admin"); }
});

// CHÍNH LÀ HÀM NÀY NÈ: Dùng để xử lý khi Admin bấm nút Xóa vi phạm
app.post('/admin/delete-listing', checkAdmin, async (req, res) => {
    try {
        const pool = await poolPromise;
        await pool.request()
            .input('id', sql.Int, req.body.listing_id)
            .query("UPDATE listings SET status = 'Deleted' WHERE listing_id = @id");
        res.json({ success: true });
    } catch (err) { 
        console.error(err);
        res.json({ success: false, message: 'Lỗi khi xóa sản phẩm!' }); 
    }
});

// --- ROUTE TRANG CHỦ & CHI TIẾT ---
app.get('/', async (req, res) => {
    try {
        const pool = await poolPromise;
        const queryAll = `SELECT l.listing_id AS id, l.title, l.price, l.condition_percentage AS condition, l.location_gps AS location, CASE WHEN i.image_url LIKE 'http%' THEN i.image_url ELSE '/images/' + ISNULL(i.image_url, 'default.jpg') END AS img FROM listings l LEFT JOIN listing_images i ON l.listing_id = i.listing_id AND i.is_thumbnail = 1 WHERE l.status = 'Active' ORDER BY l.is_vip DESC, l.created_at DESC`;
        const resultAll = await pool.request().query(queryAll);
        const listings = resultAll.recordset.map(item => ({ ...item, price: formatPrice(item.price) }));

        let viewedItems = [];
        if (req.session.user && req.session.recentlyViewed.length > 0) {
            const validIds = req.session.recentlyViewed.map(id => parseInt(id)).filter(id => !isNaN(id));
            if (validIds.length > 0) {
                const queryViewed = `SELECT l.listing_id AS id, l.title, l.price, l.condition_percentage AS condition, l.location_gps AS location, CASE WHEN i.image_url LIKE 'http%' THEN i.image_url ELSE '/images/' + ISNULL(i.image_url, 'default.jpg') END AS img FROM listings l LEFT JOIN listing_images i ON l.listing_id = i.listing_id AND i.is_thumbnail = 1 WHERE l.listing_id IN (${validIds.join(',')}) AND l.status = 'Active'`;
                const resultViewed = await pool.request().query(queryViewed);
                let rawViewed = validIds.map(id => resultViewed.recordset.find(item => item.id === id)).filter(item => item !== undefined);
                viewedItems = rawViewed.map(item => ({ ...item, price: formatPrice(item.price) }));
            }
        }
        res.render('home', { listings, viewedItems });
    } catch (err) { res.render('home', { listings: [], viewedItems: [] }); }
});

app.get('/product/:id', async (req, res) => {
    const productId = req.params.id;
    if (req.session.user) {
        req.session.recentlyViewed = req.session.recentlyViewed.filter(id => id !== productId);
        req.session.recentlyViewed.unshift(productId);
        if (req.session.recentlyViewed.length > 4) req.session.recentlyViewed.pop();
    }
    try {
        const pool = await poolPromise;
        const query = `SELECT l.listing_id AS id, l.title, l.price, l.condition_percentage AS condition, l.location_gps AS location, l.description, u.full_name AS seller_name, CASE WHEN i.image_url LIKE 'http%' THEN i.image_url ELSE '/images/' + ISNULL(i.image_url, 'default.jpg') END AS img FROM listings l LEFT JOIN users u ON l.seller_id = u.user_id LEFT JOIN listing_images i ON l.listing_id = i.listing_id AND i.is_thumbnail = 1 WHERE l.listing_id = @id AND l.status = 'Active'`;
        const result = await pool.request().input('id', sql.Int, productId).query(query);
        if (result.recordset.length > 0) {
            let product = result.recordset[0];
            product.price = formatPrice(product.price); 
            res.render('product-detail', { product });
        } else {
            res.status(404).send('Không tìm thấy sản phẩm hoặc đã bị gỡ.');
        }
    } catch (err) { res.status(500).send('Lỗi máy chủ'); }
});

// --- ROUTE QUẢN LÝ TIN ĐĂNG CỦA TÔI ---
app.get('/my-listings', checkLogin, async (req, res) => {
    try {
        const pool = await poolPromise;
        const query = `SELECT l.listing_id AS id, l.title, l.price, CASE WHEN i.image_url LIKE 'http%' THEN i.image_url ELSE '/images/' + ISNULL(i.image_url, 'default.jpg') END AS img FROM listings l LEFT JOIN listing_images i ON l.listing_id = i.listing_id AND i.is_thumbnail = 1 WHERE l.seller_id = @seller_id AND l.status != 'Deleted' ORDER BY l.created_at DESC`;
        const result = await pool.request().input('seller_id', sql.Int, req.session.user.user_id).query(query);
        const myListings = result.recordset.map(item => ({ ...item, price: formatPrice(item.price) }));
        res.render('my-listings', { myListings });
    } catch (err) { res.status(500).send("Lỗi tải danh sách tin đăng."); }
});

app.post('/my-listings/delete', checkLogin, async (req, res) => {
    try {
        const pool = await poolPromise;
        await pool.request().input('id', sql.Int, req.body.listing_id).input('seller_id', sql.Int, req.session.user.user_id).query("UPDATE listings SET status = 'Deleted' WHERE listing_id = @id AND seller_id = @seller_id");
        res.json({ success: true });
    } catch (err) { res.json({ success: false, message: 'Lỗi khi xóa sản phẩm!' }); }
});

// --- ROUTE GIỎ HÀNG ---
app.post('/cart/add', (req, res) => {
    if (!req.session.user) return res.json({ success: false, message: 'Vui lòng đăng nhập!', redirect: '/login' });
    const { listing_id } = req.body;
    if (!req.session.cart.includes(listing_id)) req.session.cart.push(listing_id);
    res.json({ success: true, cartCount: req.session.cart.length });
});

app.get('/cart', async (req, res) => {
    if (!req.session.cart || req.session.cart.length === 0) return res.render('cart', { cartItems: [] });
    try {
        const pool = await poolPromise;
        const validIds = req.session.cart.map(id => parseInt(id)).filter(id => !isNaN(id));
        const query = `SELECT l.listing_id AS id, l.title, l.price, CASE WHEN i.image_url LIKE 'http%' THEN i.image_url ELSE '/images/' + ISNULL(i.image_url, 'default.jpg') END AS img FROM listings l LEFT JOIN listing_images i ON l.listing_id = i.listing_id AND i.is_thumbnail = 1 WHERE l.listing_id IN (${validIds.join(',')}) AND l.status = 'Active'`;
        const result = await pool.request().query(query);
        const cartItems = result.recordset.map(item => ({ ...item, price: formatPrice(item.price) }));
        res.render('cart', { cartItems });
    } catch (err) { res.render('cart', { cartItems: [] }); }
});

app.post('/cart/remove', (req, res) => {
    req.session.cart = req.session.cart.filter(id => id !== req.body.listing_id);
    res.redirect('/cart');
});

// --- ROUTE TÀI KHOẢN ---
app.get('/login', (req, res) => res.render('login'));
app.post('/login', async (req, res) => {
    try {
        const pool = await poolPromise;
        const result = await pool.request().input('email', sql.NVarChar, req.body.email).input('password', sql.NVarChar, req.body.password).query('SELECT * FROM users WHERE email = @email AND password_hash = @password');
        if (result.recordset.length > 0) {
            req.session.user = result.recordset[0]; 
            showPopup(res, 'Thành công!', `Chào mừng ${result.recordset[0].full_name} trở lại!`, 'success', '/');
        } else { showPopup(res, 'Thất bại!', 'Sai Email hoặc mật khẩu!', 'error', '/login'); }
    } catch (err) { showPopup(res, 'Lỗi hệ thống!', 'Vui lòng thử lại sau.', 'error', '/login'); }
});

app.get('/register', (req, res) => res.render('register'));
app.post('/register', async (req, res) => {
    try {
        const pool = await poolPromise;
        await pool.request().input('fullname', sql.NVarChar, req.body.fullname).input('phone', sql.NVarChar, req.body.phone_number).input('email', sql.NVarChar, req.body.email).input('password', sql.NVarChar, req.body.password).query('INSERT INTO users (full_name, phone_number, email, password_hash) VALUES (@fullname, @phone, @email, @password)');
        showPopup(res, 'Tuyệt vời!', 'Đăng ký tài khoản thành công!', 'success', '/login');
    } catch (err) { showPopup(res, 'Lỗi đăng ký!', 'Email này có thể đã được sử dụng.', 'error', '/register'); }
});
app.get('/logout', (req, res) => { req.session.destroy(); res.redirect('/'); });

// --- ROUTE ĐĂNG TIN ---
app.get('/post-ad', checkLogin, async (req, res) => {
    try {
        const pool = await poolPromise;
        const categories = await pool.request().query('SELECT * FROM categories');
        res.render('post-ad', { categories: categories.recordset });
    } catch (err) { res.render('post-ad', { categories: [] }); }
});
app.post('/post-ad', checkLogin, upload.single('image_file'), async (req, res) => {
    try {
        const { title, category_id, price, condition, location, description, image_url } = req.body;
        let finalImage = 'default.jpg';
        if (req.file) finalImage = req.file.filename;
        else if (image_url && image_url.trim() !== '') finalImage = image_url;

        const pool = await poolPromise;
        const listingResult = await pool.request().input('seller_id', sql.Int, req.session.user.user_id).input('category_id', sql.Int, category_id || 1).input('title', sql.NVarChar, title).input('description', sql.NVarChar, description).input('price', sql.Decimal, price).input('condition', sql.Int, condition).input('location', sql.NVarChar, location).query(`INSERT INTO listings (seller_id, category_id, title, description, price, condition_percentage, location_gps, status) OUTPUT INSERTED.listing_id VALUES (@seller_id, @category_id, @title, @description, @price, @condition, @location, 'Active')`);
        await pool.request().input('listing_id', sql.Int, listingResult.recordset[0].listing_id).input('image_url', sql.NVarChar, finalImage).query(`INSERT INTO listing_images (listing_id, image_url, is_thumbnail) VALUES (@listing_id, @image_url, 1)`);
            
        showPopup(res, 'Thành công!', 'Sản phẩm của bạn đã được đăng lên sàn!', 'success', '/');
    } catch (err) { showPopup(res, 'Đăng tin thất bại!', 'Vui lòng kiểm tra lại thông tin.', 'error', 'back'); }
});

app.listen(PORT, () => console.log(`🚀 Hệ thống SHM đang chạy tại: http://localhost:${PORT}`));