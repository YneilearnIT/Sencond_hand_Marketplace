const express = require('express');
const path = require('path');
const { engine } = require('express-handlebars');
const cookieParser = require('cookie-parser');
const session = require('express-session');
const { sql, poolPromise } = require('./src/config/db');

const app = express();
const PORT = 3000;

// --- HÀM TẠO POPUP THÔNG BÁO ĐẸP (SWEETALERT2) ---
const showPopup = (res, title, text, icon, redirectUrl) => {
    const action = redirectUrl === 'back' ? 'window.history.back();' : `window.location.href = '${redirectUrl}';`;
    res.send(`
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
            <style>body { background-color: #f8f9fa; font-family: sans-serif; }</style>
        </head>
        <body>
            <script>
                Swal.fire({
                    title: '${title}',
                    text: '${text}',
                    icon: '${icon}',
                    confirmButtonColor: '#0d6efd',
                    confirmButtonText: 'OK',
                    allowOutsideClick: false
                }).then(() => {
                    ${action}
                });
            </script>
        </body>
        </html>
    `);
};

app.use(session({
    secret: 'shm_secret_key_2026', resave: false, saveUninitialized: true, cookie: { secure: false } 
}));

// Middleware kiểm tra đăng nhập bằng Popup đẹp
const checkLogin = (req, res, next) => {
    if (req.session.user) next();
    else showPopup(res, 'Cảnh báo!', 'Bạn cần đăng nhập để sử dụng tính năng này!', 'warning', '/login');
};

app.use((req, res, next) => {
    res.locals.user = req.session.user || null;
    if (!req.session.cart) req.session.cart = [];
    if (!req.session.recentlyViewed) req.session.recentlyViewed = [];
    res.locals.cartItemCount = req.session.cart.length; 
    next();
});

app.engine('hbs', engine({ extname: '.hbs', defaultLayout: 'main', layoutsDir: path.join(__dirname, 'views/layouts') }));
app.set('view engine', 'hbs');
app.set('views', path.join(__dirname, 'views'));

app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

// --- ROUTE TRANG CHỦ & CHI TIẾT ---
app.get('/', async (req, res) => {
    try {
        const pool = await poolPromise;
        const queryAll = `SELECT l.listing_id AS id, l.title, l.price, l.condition_percentage AS condition, l.location_gps AS location, '/images/' + ISNULL(i.image_url, 'default.jpg') AS img FROM listings l LEFT JOIN listing_images i ON l.listing_id = i.listing_id AND i.is_thumbnail = 1 WHERE l.status = 'Active' ORDER BY l.is_vip DESC, l.created_at DESC`;
        const resultAll = await pool.request().query(queryAll);

        let viewedItems = [];
        if (req.session.user && req.session.recentlyViewed.length > 0) {
            const validIds = req.session.recentlyViewed.map(id => parseInt(id)).filter(id => !isNaN(id));
            if (validIds.length > 0) {
                const queryViewed = `SELECT l.listing_id AS id, l.title, l.price, l.condition_percentage AS condition, l.location_gps AS location, '/images/' + ISNULL(i.image_url, 'default.jpg') AS img FROM listings l LEFT JOIN listing_images i ON l.listing_id = i.listing_id AND i.is_thumbnail = 1 WHERE l.listing_id IN (${validIds.join(',')})`;
                const resultViewed = await pool.request().query(queryViewed);
                viewedItems = validIds.map(id => resultViewed.recordset.find(item => item.id === id)).filter(item => item !== undefined);
            }
        }
        res.render('home', { listings: resultAll.recordset, viewedItems: viewedItems });
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
        const query = `SELECT l.listing_id AS id, l.title, l.price, l.condition_percentage AS condition, l.location_gps AS location, l.description, u.full_name AS seller_name, '/images/' + ISNULL(i.image_url, 'default.jpg') AS img FROM listings l LEFT JOIN users u ON l.seller_id = u.user_id LEFT JOIN listing_images i ON l.listing_id = i.listing_id AND i.is_thumbnail = 1 WHERE l.listing_id = @id`;
        const result = await pool.request().input('id', sql.Int, productId).query(query);
        if (result.recordset.length > 0) res.render('product-detail', { product: result.recordset[0] });
        else res.status(404).send('Không tìm thấy sản phẩm');
    } catch (err) { res.status(500).send('Lỗi máy chủ'); }
});

// --- ROUTE GIỎ HÀNG (AJAX) ---
app.post('/cart/add', (req, res) => {
    const { listing_id } = req.body;
    if (!req.session.cart.includes(listing_id)) req.session.cart.push(listing_id);
    res.json({ success: true, cartCount: req.session.cart.length });
});

app.get('/cart', async (req, res) => {
    if (!req.session.cart || req.session.cart.length === 0) return res.render('cart', { cartItems: [] });
    try {
        const pool = await poolPromise;
        const validIds = req.session.cart.map(id => parseInt(id)).filter(id => !isNaN(id));
        const query = `SELECT l.listing_id AS id, l.title, l.price, '/images/' + ISNULL(i.image_url, 'default.jpg') AS img FROM listings l LEFT JOIN listing_images i ON l.listing_id = i.listing_id AND i.is_thumbnail = 1 WHERE l.listing_id IN (${validIds.join(',')})`;
        const result = await pool.request().query(query);
        res.render('cart', { cartItems: result.recordset });
    } catch (err) { res.render('cart', { cartItems: [] }); }
});

app.post('/cart/remove', (req, res) => {
    req.session.cart = req.session.cart.filter(id => id !== req.body.listing_id);
    res.redirect('/cart');
});

// --- ROUTE TÀI KHOẢN (Đã dùng Popup Đẹp) ---
app.get('/login', (req, res) => res.render('login'));
app.post('/login', async (req, res) => {
    try {
        const pool = await poolPromise;
        const result = await pool.request().input('email', sql.NVarChar, req.body.email).input('password', sql.NVarChar, req.body.password).query('SELECT * FROM users WHERE email = @email AND password_hash = @password');
        if (result.recordset.length > 0) {
            req.session.user = result.recordset[0]; 
            showPopup(res, 'Thành công!', `Chào mừng ${result.recordset[0].full_name} trở lại!`, 'success', '/');
        } else { 
            showPopup(res, 'Thất bại!', 'Sai Email hoặc mật khẩu!', 'error', '/login'); 
        }
    } catch (err) { showPopup(res, 'Lỗi hệ thống!', 'Vui lòng thử lại sau.', 'error', '/login'); }
});

app.get('/register', (req, res) => res.render('register'));
app.post('/register', async (req, res) => {
    try {
        const pool = await poolPromise;
        await pool.request().input('fullname', sql.NVarChar, req.body.fullname).input('phone', sql.NVarChar, req.body.phone_number).input('email', sql.NVarChar, req.body.email).input('password', sql.NVarChar, req.body.password).query('INSERT INTO users (full_name, phone_number, email, password_hash) VALUES (@fullname, @phone, @email, @password)');
        showPopup(res, 'Tuyệt vời!', 'Đăng ký tài khoản thành công!', 'success', '/login');
    } catch (err) { 
        showPopup(res, 'Lỗi đăng ký!', 'Email này có thể đã được sử dụng.', 'error', '/register'); 
    }
});
app.get('/logout', (req, res) => { req.session.destroy(); res.redirect('/'); });

// --- ROUTE ĐĂNG TIN (Đã dùng Popup Đẹp) ---
app.get('/post-ad', checkLogin, async (req, res) => {
    try {
        const pool = await poolPromise;
        const categories = await pool.request().query('SELECT * FROM categories');
        res.render('post-ad', { categories: categories.recordset });
    } catch (err) { res.render('post-ad', { categories: [] }); }
});
app.post('/post-ad', checkLogin, async (req, res) => {
    try {
        const pool = await poolPromise;
        const listingResult = await pool.request().input('seller_id', sql.Int, req.session.user.user_id).input('category_id', sql.Int, req.body.category_id).input('title', sql.NVarChar, req.body.title).input('description', sql.NVarChar, req.body.description).input('price', sql.Decimal, req.body.price).input('condition', sql.Int, req.body.condition).input('location', sql.NVarChar, req.body.location).query(`INSERT INTO listings (seller_id, category_id, title, description, price, condition_percentage, location_gps, status) OUTPUT INSERTED.listing_id VALUES (@seller_id, @category_id, @title, @description, @price, @condition, @location, 'Active')`);
        await pool.request().input('listing_id', sql.Int, listingResult.recordset[0].listing_id).input('image_url', sql.NVarChar, req.body.image_url || 'default.jpg').query(`INSERT INTO listing_images (listing_id, image_url, is_thumbnail) VALUES (@listing_id, @image_url, 1)`);
        showPopup(res, 'Thành công!', 'Sản phẩm của bạn đã được đăng lên sàn!', 'success', '/');
    } catch (err) { 
        showPopup(res, 'Đăng tin thất bại!', 'Vui lòng kiểm tra lại thông tin.', 'error', 'back'); 
    }
});

app.listen(PORT, () => console.log(`🚀 Hệ thống SHM đang chạy tại: http://localhost:${PORT}`));