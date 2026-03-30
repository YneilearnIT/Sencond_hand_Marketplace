const express = require('express');
const path = require('path');
const { engine } = require('express-handlebars');
const cookieParser = require('cookie-parser');
const session = require('express-session');
const { sql, poolPromise } = require('./src/config/db');
const multer = require('multer');
const fs = require('fs');
const cors = require('cors');

const app = express();
const PORT = 3000;

// --- CẤU HÌNH ---
app.use(cors());
const uploadDir = path.join(__dirname, 'public/images');
if (!fs.existsSync(uploadDir)) fs.mkdirSync(uploadDir, { recursive: true });

const storage = multer.diskStorage({
    destination: (req, file, cb) => cb(null, uploadDir),
    filename: (req, file, cb) => {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        cb(null, 'shm-' + uniqueSuffix + path.extname(file.originalname));
    }
});
const upload = multer({ storage });

// --- HELPERS ---
const showPopup = (res, title, text, icon, redirectUrl) => {
    const action = redirectUrl === 'back' ? 'window.history.back();' : `window.location.href = '${redirectUrl}';`;
    res.send(`<!DOCTYPE html><html><head><meta charset="UTF-8"><script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script></head><body><script>Swal.fire({title:'${title}',text:'${text}',icon:'${icon}',confirmButtonColor:'#0d6efd'}).then(()=>{${action}});</script></body></html>`);
};
const formatPrice = (p) => new Intl.NumberFormat('en-US').format(p);

// --- MIDDLEWARE ---
app.use(session({ secret: 'shm_secret_2026', resave: false, saveUninitialized: true }));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use(async (req, res, next) => {
    res.locals.user = req.session.user || null;
    res.locals.isAdmin = req.session.user && req.session.user.role === 'admin';
    
    try {
        const pool = await poolPromise;
        // 1. Lấy danh mục cho Header
        const catRes = await pool.request().query("SELECT * FROM categories ORDER BY category_name ASC");
        res.locals.globalCategories = catRes.recordset;

        // 2. Lấy số lượng giỏ hàng TỪ DATABASE (Thay vì session)
        if (req.session.user) {
            const cartCount = await pool.request()
                .input('uid', sql.Int, req.session.user.user_id)
                .query("SELECT COUNT(*) as total FROM cart WHERE user_id = @uid");
            res.locals.cartItemCount = cartCount.recordset[0].total;
        } else {
            res.locals.cartItemCount = 0;
        }
    } catch (err) { 
        res.locals.globalCategories = []; 
        res.locals.cartItemCount = 0;
    }
    next();
});

const checkAdmin = (req, res, next) => (req.session.user && req.session.user.role === 'admin') ? next() : res.redirect('/');
const checkLogin = (req, res, next) => req.session.user ? next() : res.redirect('/login');

app.engine('hbs', engine({ extname: '.hbs', defaultLayout: 'main', helpers: { eq: (a, b) => a == b } }));
app.set('view engine', 'hbs');

// ==========================================
// ROUTES - AUTHENTICATION
// ==========================================
app.get('/login', (req, res) => res.render('login'));
app.post('/login', async (req, res) => {
    try {
        const pool = await poolPromise;
        const result = await pool.request()
            .input('e', sql.NVarChar, req.body.email)
            .input('p', sql.NVarChar, req.body.password)
            .query('SELECT * FROM users WHERE email=@e AND password_hash=@p');
        if (result.recordset.length) { 
            req.session.user = result.recordset[0]; 
            res.redirect('/'); 
        } else showPopup(res, 'Lỗi', 'Sai tài khoản!', 'error', '/login');
    } catch (err) { res.redirect('/login'); }
});
app.get('/register', (req, res) => res.render('register'));
app.post('/register', async (req, res) => {
    try {
        const { fullname, email, password, phone_number } = req.body;
        const pool = await poolPromise;
        await pool.request()
            .input('f', sql.NVarChar, fullname).input('e', sql.NVarChar, email)
            .input('p', sql.NVarChar, password).input('ph', sql.NVarChar, phone_number)
            .query("INSERT INTO users (full_name, email, password_hash, phone_number, role) VALUES (@f, @e, @p, @ph, 'user')");
        showPopup(res, 'Thành công', 'Đã tạo tài khoản!', 'success', '/login');
    } catch (err) { showPopup(res, 'Lỗi', 'Email hoặc SĐT đã tồn tại!', 'error', '/register'); }
});
app.get('/logout', (req, res) => { req.session.destroy(); res.redirect('/'); });

// ==========================================
// ROUTES - MẶT HÀNG (WEB)
// ==========================================
app.get('/', async (req, res) => {
    try {
        const pool = await poolPromise;
        const { keyword = '', category = '' } = req.query;
        let query = `SELECT l.listing_id AS id, l.title, l.price, l.is_vip, CASE WHEN i.image_url LIKE 'http%' THEN i.image_url ELSE '/images/' + ISNULL(i.image_url, 'default.jpg') END AS img FROM listings l LEFT JOIN listing_images i ON l.listing_id = i.listing_id AND i.is_thumbnail = 1 WHERE l.status = 'Active'`;
        const request = pool.request();
        if (keyword.trim()) { query += ` AND l.title LIKE @key`; request.input('key', sql.NVarChar, `%${keyword}%`); }
        if (category) { query += ` AND l.category_id = @cat`; request.input('cat', sql.Int, category); }
        query += ` ORDER BY l.is_vip DESC, l.created_at DESC`;
        const result = await request.query(query);
        res.render('home', { listings: result.recordset.map(i => ({ ...i, price: formatPrice(i.price) })), keyword, selectedCategory: category });
    } catch (err) { res.render('home', { listings: [] }); }
});

app.get('/product/:id', async (req, res) => {
    try {
        const pool = await poolPromise;
        const result = await pool.request().input('id', sql.Int, req.params.id).query(`
            SELECT l.*, u.full_name AS seller_name, u.phone_number,
            CASE WHEN i.image_url LIKE 'http%' THEN i.image_url ELSE '/images/' + ISNULL(i.image_url, 'default.jpg') END AS img 
            FROM listings l LEFT JOIN users u ON l.seller_id = u.user_id 
            LEFT JOIN listing_images i ON l.listing_id = i.listing_id AND i.is_thumbnail = 1 
            WHERE l.listing_id = @id AND l.status != 'Deleted'
        `);
        if (result.recordset.length) {
            let p = result.recordset[0];
            p.price = formatPrice(p.price);
            res.render('product-detail', { product: p });
        } else res.redirect('/');
    } catch (err) { res.redirect('/'); }
});

// ==========================================
// ROUTES - GIỎ HÀNG (DATABASE)
// ==========================================
app.get('/cart', checkLogin, async (req, res) => {
    try {
        const pool = await poolPromise;
        const result = await pool.request()
            .input('uid', sql.Int, req.session.user.user_id)
            .query(`SELECT l.*, CASE WHEN i.image_url LIKE 'http%' THEN i.image_url ELSE '/images/' + ISNULL(i.image_url, 'default.jpg') END AS img 
                    FROM cart c JOIN listings l ON c.listing_id = l.listing_id 
                    LEFT JOIN listing_images i ON l.listing_id = i.listing_id AND i.is_thumbnail = 1 
                    WHERE c.user_id = @uid AND l.status != 'Deleted'`);
        res.render('cart', { cartItems: result.recordset.map(i => ({ ...i, price: formatPrice(i.price) })) });
    } catch (err) { res.render('cart', { cartItems: [] }); }
});

app.post('/cart/add', async (req, res) => {
    if (!req.session.user) return res.json({ success: false, message: 'Vui lòng đăng nhập!', redirect: '/login' });
    try {
        const { listing_id } = req.body;
        const pool = await poolPromise;
        const check = await pool.request().input('uid', sql.Int, req.session.user.user_id).input('lid', sql.Int, listing_id).query("SELECT * FROM cart WHERE user_id=@uid AND listing_id=@lid");
        if (check.recordset.length === 0) {
            await pool.request().input('uid', sql.Int, req.session.user.user_id).input('lid', sql.Int, listing_id).query("INSERT INTO cart (user_id, listing_id) VALUES (@uid, @lid)");
        }
        const count = await pool.request().input('uid', sql.Int, req.session.user.user_id).query("SELECT COUNT(*) as total FROM cart WHERE user_id=@uid");
        res.json({ success: true, cartCount: count.recordset[0].total });
    } catch (err) { res.json({ success: false }); }
});
// Route XÓA SẢN PHẨM KHỎI GIỎ HÀNG (Lưu vĩnh viễn vào SQL)
app.post('/cart/remove', checkLogin, async (req, res) => {
    try {
        const { listing_id } = req.body;
        const userId = req.session.user.user_id;
        const pool = await poolPromise;

        // Xóa dòng tương ứng trong bảng cart của SQL Server
        await pool.request()
            .input('uid', sql.Int, userId)
            .input('lid', sql.Int, listing_id)
            .query("DELETE FROM cart WHERE user_id = @uid AND listing_id = @lid");

        console.log(`✅ Đã xóa listing ${listing_id} khỏi giỏ hàng của user ${userId}`);
        res.json({ success: true });
    } catch (err) {
        console.error("Lỗi xóa giỏ hàng:", err);
        res.json({ success: false, message: "Không thể xóa sản phẩm khỏi giỏ hàng" });
    }
});
// ==========================================
// ROUTES - QUẢN LÝ TIN & ADMIN
// ==========================================
app.get('/my-listings', checkLogin, async (req, res) => {
    try {
        const pool = await poolPromise;
        const result = await pool.request().input('uid', sql.Int, req.session.user.user_id).query(`SELECT l.listing_id as id, l.*, CASE WHEN i.image_url LIKE 'http%' THEN i.image_url ELSE '/images/' + ISNULL(i.image_url, 'default.jpg') END AS img FROM listings l LEFT JOIN listing_images i ON l.listing_id = i.listing_id AND i.is_thumbnail = 1 WHERE l.seller_id = @uid AND l.status != 'Deleted' ORDER BY l.created_at DESC`);
        res.render('my-listings', { myListings: result.recordset.map(i => ({ ...i, price: formatPrice(i.price) })) });
    } catch (err) { res.send("Lỗi tải danh sách tin"); }
});

app.post('/post-ad', checkLogin, upload.single('image_file'), async (req, res) => {
    try {
        const { title, category_id, price, description, condition, location } = req.body;
        const pool = await poolPromise;
        const result = await pool.request()
            .input('sid', sql.Int, req.session.user.user_id).input('cid', sql.Int, category_id)
            .input('t', sql.NVarChar, title).input('d', sql.NVarChar, description)
            .input('p', sql.Decimal, price).input('con', sql.Int, condition).input('loc', sql.NVarChar, location)
            .query(`INSERT INTO listings (seller_id, category_id, title, description, price, condition_percentage, location_gps, status) OUTPUT INSERTED.listing_id VALUES (@sid, @cid, @t, @d, @p, @con, @loc, 'Pending')`);
        const img = req.file ? req.file.filename : 'default.jpg';
        await pool.request().input('lid', sql.Int, result.recordset[0].listing_id).input('url', sql.NVarChar, img).query(`INSERT INTO listing_images (listing_id, image_url, is_thumbnail) VALUES (@lid, @url, 1)`);
        showPopup(res, 'Thành công', 'Tin đang chờ Admin duyệt!', 'info', '/my-listings');
    } catch (err) { showPopup(res, 'Lỗi', 'Lỗi đăng tin!', 'error', 'back'); }
});

app.post('/delete-listing', checkLogin, async (req, res) => {
    try {
        await (await poolPromise).request().input('id', sql.Int, req.body.listing_id).query("UPDATE listings SET status = 'Deleted' WHERE listing_id = @id");
        res.json({ success: true });
    } catch (err) { res.json({ success: false }); }
});

app.get('/admin', checkAdmin, async (req, res) => {
    try {
        const pool = await poolPromise;
        const ads = await pool.request().query("SELECT l.*, u.full_name FROM listings l JOIN users u ON l.seller_id = u.user_id WHERE l.status = 'Pending'");
        const vips = await pool.request().query("SELECT l.*, u.full_name FROM listings l JOIN users u ON l.seller_id = u.user_id WHERE l.is_pending_vip = 1");
        const all = await pool.request().query("SELECT l.*, u.full_name FROM listings l JOIN users u ON l.seller_id = u.user_id WHERE l.status = 'Active' ORDER BY l.is_vip DESC");
        res.render('admin-dashboard', { pendingAds: ads.recordset, pendingVip: vips.recordset, allListings: all.recordset.map(i => ({...i, price: formatPrice(i.price)})) });
    } catch (err) { res.status(500).send("Lỗi Admin"); }
});

app.post('/admin/approve-ad', checkAdmin, async (req, res) => {
    try {
        await (await poolPromise).request().input('id', sql.Int, req.body.listing_id).query("UPDATE listings SET status = 'Active' WHERE listing_id = @id");
        res.json({ success: true });
    } catch (err) { res.json({ success: false }); }
});

app.post('/admin/approve-vip', checkAdmin, async (req, res) => {
    try {
        await (await poolPromise).request().input('id', sql.Int, req.body.listing_id).query("UPDATE listings SET is_vip = 1, is_pending_vip = 0 WHERE listing_id = @id");
        res.json({ success: true });
    } catch (err) { res.json({ success: false }); }
});

// ==========================================
// API DÀNH CHO MOBILE (JSON ONLY)
// ==========================================
app.get('/api/categories', async (req, res) => {
    try {
        const pool = await poolPromise;
        const result = await pool.request().query("SELECT * FROM categories ORDER BY category_name ASC");
        res.json({ success: true, data: result.recordset });
    } catch (err) { res.status(500).json({ success: false, error: err.message }); }
});

app.get('/api/listings', async (req, res) => {
    try {
        const { keyword = '', category_id = '' } = req.query;
        const pool = await poolPromise;
        let query = `SELECT l.*, i.image_url FROM listings l LEFT JOIN listing_images i ON l.listing_id = i.listing_id AND i.is_thumbnail = 1 WHERE l.status = 'Active'`;
        if (keyword) query += ` AND l.title LIKE '%${keyword}%'`;
        if (category_id) query += ` AND l.category_id = ${category_id}`;
        const result = await pool.request().query(query + " ORDER BY l.is_vip DESC");
        res.json({ success: true, data: result.recordset });
    } catch (err) { res.status(500).json({ success: false }); }
});

app.listen(PORT, () => console.log(`🚀 SERVER READY: http://localhost:${PORT}`));