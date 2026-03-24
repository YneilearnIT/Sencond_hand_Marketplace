const express = require('express');
const path = require('path');
const { engine } = require('express-handlebars');
const cookieParser = require('cookie-parser');
const session = require('express-session');
const apiRoutes = require('./src/routes/api');
const pool = require('./src/config/db');

const app = express();
const PORT = 3000;

// 1. Cấu hình Session
app.use(session({
    secret: 'shm_secret_key_2026', 
    resave: false,
    saveUninitialized: true,
    cookie: { secure: false } 
}));

// 2. Middleware kiểm tra đăng nhập
const checkLogin = (req, res, next) => {
    if (req.session.user) {
        next();
    } else {
        res.send(`
            <script>
                alert('Bạn cần đăng nhập để sử dụng tính năng này!');
                window.location.href = '/login';
            </script>
        `);
    }
};

// 3. Middleware truyền thông tin user vào View
app.use((req, res, next) => {
    res.locals.user = req.session.user || null;
    next();
});

// 4. Cấu hình View Engine (Handlebars)
app.engine('hbs', engine({ 
    extname: '.hbs', 
    defaultLayout: 'main', 
    layoutsDir: path.join(__dirname, 'views/layouts') 
}));
app.set('view engine', 'hbs');
app.set('views', path.join(__dirname, 'views'));

app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/api', apiRoutes);

// --- CÁC ROUTE GIAO DIỆN ---

// Trang chủ - ĐÃ SỬA QUERY ĐỂ HIỆN ẢNH
app.get('/', async (req, res) => {
    try {
        const query = `
            SELECT l.listing_id AS id, l.title, l.price, l.condition_percentage AS condition, 
                   l.location_gps AS location, 
                   CASE 
                       WHEN i.image_url LIKE 'http%' THEN i.image_url 
                       ELSE '/images/' || i.image_url 
                   END AS img
            FROM listings l
            LEFT JOIN listing_images i ON l.listing_id = i.listing_id AND i.is_thumbnail = TRUE
            WHERE l.status = 'Active'
            ORDER BY l.is_vip DESC, l.created_at DESC LIMIT 8
        `;
        const result = await pool.query(query);
        res.render('home', { listings: result.rows });
    } catch (err) {
        console.error(err);
        res.render('home', { listings: [] });
    }
});

// Đăng nhập
app.get('/login', (req, res) => res.render('login'));
app.post('/login', async (req, res) => {
    const { email, password } = req.body;
    try {
        const query = 'SELECT * FROM users WHERE email = $1 AND password_hash = $2';
        const result = await pool.query(query, [email, password]);
        if (result.rows.length > 0) {
            req.session.user = result.rows[0]; 
            res.send(`<script>alert('Chào mừng ${result.rows[0].full_name}!'); window.location.href = '/';</script>`);
        } else {
            res.send(`<script>alert('Sai Email hoặc mật khẩu!'); window.location.href = '/login';</script>`);
        }
    } catch (err) { res.status(500).send("Lỗi hệ thống."); }
});

// Đăng ký
app.get('/register', (req, res) => res.render('register'));
app.post('/register', async (req, res) => {
    const { fullname, phone_number, email, password } = req.body;
    try {
        await pool.query(
            'INSERT INTO users (full_name, phone_number, email, password_hash) VALUES ($1, $2, $3, $4)', 
            [fullname, phone_number, email, password]
        );
        res.send(`<script>alert('Đăng ký thành công!'); window.location.href = '/login';</script>`);
    } catch (err) { 
        console.error(err);
        res.send(`<script>alert('Lỗi: ${err.message}'); window.location.href = '/register';</script>`); 
    }
});

// Đăng xuất
app.get('/logout', (req, res) => {
    req.session.destroy();
    res.redirect('/');
});

// Giao diện Đăng tin
app.get('/post-ad', checkLogin, async (req, res) => {
    try {
        const categories = await pool.query('SELECT * FROM categories');
        res.render('post-ad', { categories: categories.rows });
    } catch (err) { res.render('post-ad', { categories: [] }); }
});

// Xử lý Đăng tin
app.post('/post-ad', checkLogin, async (req, res) => {
    const { title, category_id, price, condition, location, description, image_url } = req.body;
    const seller_id = req.session.user.user_id;

    try {
        const listingResult = await pool.query(
            `INSERT INTO listings (seller_id, category_id, title, description, price, condition_percentage, location_gps, status) 
             VALUES ($1, $2, $3, $4, $5, $6, $7, 'Active') RETURNING listing_id`,
            [seller_id, category_id, title, description, price, condition, location]
        );

        const newListingId = listingResult.rows[0].listing_id;

        await pool.query(
            `INSERT INTO listing_images (listing_id, image_url, is_thumbnail) VALUES ($1, $2, TRUE)`,
            [newListingId, image_url || 'default.jpg']
        );

        res.send(`<script>alert('Đăng tin thành công!'); window.location.href = '/';</script>`);
    } catch (err) { 
        console.error(err);
        res.send(`<script>alert('Lỗi khi đăng tin!'); window.history.back();</script>`); 
    }
});

app.listen(PORT, () => console.log(`🚀 Hệ thống SHM đang chạy tại: http://localhost:${PORT}`));