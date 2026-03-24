const express = require('express');
const path = require('path');
const { engine } = require('express-handlebars');
const cookieParser = require('cookie-parser');
const session = require('express-session');
const apiRoutes = require('./src/routes/api');
const { sql, poolPromise } = require('./src/config/db'); 

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

// Trang chủ (Tích hợp Tìm kiếm & Lọc theo Danh mục)
app.get('/', async (req, res) => {
    const { search, category } = req.query; // Lấy từ khóa và danh mục từ URL
    
    try {
        const pool = await poolPromise;
        const request = pool.request();
        
        // 1. Lấy danh sách danh mục để hiển thị lên thanh Menu lọc
        const catResult = await request.query('SELECT * FROM categories');

        // 2. Xây dựng câu lệnh truy vấn sản phẩm động
        let query = `
            SELECT l.listing_id AS id, l.title, l.price, l.condition_percentage AS condition, 
                   l.location_gps AS location, i.image_url AS img
            FROM listings l
            LEFT JOIN listing_images i ON l.listing_id = i.listing_id AND i.is_thumbnail = 1
            WHERE l.status = 'Active'
        `;

        // Nếu người dùng có gõ tìm kiếm
        if (search) {
            query += ` AND l.title LIKE @search`;
            request.input('search', sql.NVarChar, `%${search}%`);
        }

        // Nếu người dùng có bấm chọn 1 danh mục
        if (category) {
            query += ` AND l.category_id = @category`;
            request.input('category', sql.Int, category);
        }

        // Sắp xếp ưu tiên tin VIP và tin mới nhất
        query += ` ORDER BY l.is_vip DESC, l.created_at DESC`;

        const result = await request.query(query);
        
        res.render('home', { 
            listings: result.recordset, 
            categories: catResult.recordset,
            searchKeyword: search // Giữ lại từ khóa trên ô tìm kiếm
        }); 
    } catch (err) {
        console.error("Lỗi tải trang chủ:", err);
        res.render('home', { listings: [], categories: [] });
    }
});

// Xem Chi tiết Sản phẩm
app.get('/product/:id', async (req, res) => {
    const { id } = req.params;
    
    try {
        const pool = await poolPromise;
        
        // 1. Lấy thông tin chi tiết sản phẩm + Tên người bán
        const productQuery = `
            SELECT l.*, c.category_name, u.full_name AS seller_name, u.reputation_score
            FROM listings l
            JOIN categories c ON l.category_id = c.category_id
            JOIN users u ON l.seller_id = u.user_id
            WHERE l.listing_id = @id AND l.status = 'Active'
        `;
        const productResult = await pool.request()
            .input('id', sql.Int, id)
            .query(productQuery);

        if (productResult.recordset.length === 0) {
            return res.status(404).send('Sản phẩm không tồn tại hoặc đã bị ẩn/bán.');
        }

        // 2. Lấy tất cả hình ảnh của sản phẩm này
        const imageQuery = `SELECT image_url, is_thumbnail FROM listing_images WHERE listing_id = @id`;
        const imageResult = await pool.request()
            .input('id', sql.Int, id)
            .query(imageQuery);

        res.render('product', { 
            product: productResult.recordset[0],
            images: imageResult.recordset
        });
    } catch (err) {
        console.error(err);
        res.status(500).send('Lỗi máy chủ khi tải chi tiết sản phẩm.');
    }
});

// Đăng nhập
app.get('/login', (req, res) => res.render('login'));
app.post('/login', async (req, res) => {
    const { email, password } = req.body;
    try {
        const pool = await poolPromise;
        const result = await pool.request()
            .input('email', sql.VarChar, email)
            .input('password', sql.VarChar, password)
            .query('SELECT * FROM users WHERE email = @email AND password_hash = @password');
            
        if (result.recordset.length > 0) {
            req.session.user = result.recordset[0]; 
            res.send(`<script>alert('Chào mừng ${result.recordset[0].full_name}!'); window.location.href = '/';</script>`);
        } else {
            res.send(`<script>alert('Sai Email hoặc mật khẩu!'); window.location.href = '/login';</script>`);
        }
    } catch (err) { 
        console.error(err);
        res.status(500).send("Lỗi hệ thống."); 
    }
});

// Đăng ký
app.get('/register', (req, res) => res.render('register'));
app.post('/register', async (req, res) => {
    const { fullname, phone_number, email, password } = req.body;
    try {
        const pool = await poolPromise;
        await pool.request()
            .input('fullname', sql.NVarChar, fullname)
            .input('phone', sql.VarChar, phone_number)
            .input('email', sql.VarChar, email)
            .input('password', sql.VarChar, password)
            .query('INSERT INTO users (full_name, phone_number, email, password_hash) VALUES (@fullname, @phone, @email, @password)');
            
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
        const pool = await poolPromise;
        const categories = await pool.request().query('SELECT * FROM categories');
        res.render('post-ad', { categories: categories.recordset });
    } catch (err) { 
        res.render('post-ad', { categories: [] }); 
    }
});

// Xử lý Đăng tin
app.post('/post-ad', checkLogin, async (req, res) => {
    const { title, category_id, price, condition, location, description, image_url } = req.body;
    const seller_id = req.session.user.user_id; 

    try {
        const pool = await poolPromise;
        
        const listingQuery = `
            INSERT INTO listings (seller_id, category_id, title, description, price, condition_percentage, location_gps, status) 
            OUTPUT INSERTED.listing_id
            VALUES (@seller_id, @category_id, @title, @description, @price, @condition, @location, 'Active')
        `;
        
        const listingResult = await pool.request()
            .input('seller_id', sql.Int, seller_id)
            .input('category_id', sql.Int, category_id)
            .input('title', sql.NVarChar, title)
            .input('description', sql.NVarChar, description)
            .input('price', sql.Decimal(18,2), price)
            .input('condition', sql.Int, condition)
            .input('location', sql.NVarChar, location)
            .query(listingQuery);
            
        const newListingId = listingResult.recordset[0].listing_id;
        
        await pool.request()
            .input('listing_id', sql.Int, newListingId)
            .input('image_url', sql.VarChar, image_url || 'https://via.placeholder.com/300')
            .query('INSERT INTO listing_images (listing_id, image_url, is_thumbnail) VALUES (@listing_id, @image_url, 1)');
            
        res.send(`<script>alert('Đăng tin thành công!'); window.location.href = '/';</script>`);
    } catch (err) { 
        console.error(err);
        res.send(`<script>alert('Lỗi khi đăng tin!'); window.history.back();</script>`); 
    }
});

app.listen(PORT, () => console.log(`🚀 Hệ thống SHM đang chạy tại: http://localhost:${PORT}`));