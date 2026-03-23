const express = require('express');
const path = require('path');
const { engine } = require('express-handlebars');
const cookieParser = require('cookie-parser');
const apiRoutes = require('./src/routes/api');

const app = express();
const PORT = 3000;

// 1. Cấu hình View Engine (Handlebars)
app.engine('hbs', engine({
    extname: '.hbs',
    defaultLayout: 'main',
    layoutsDir: path.join(__dirname, 'views/layouts')
}));
app.set('view engine', 'hbs');
app.set('views', path.join(__dirname, 'views'));

// 2. Middleware (Xử lý dữ liệu đầu vào)
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

// 3. Kết nối API Routes (Backend)
app.use('/api', apiRoutes);

// 4. Các Route hiển thị giao diện (Frontend)
app.get('/', (req, res) => {
    res.render('home');
});

app.get('/login', (req, res) => {
    res.render('login');
});

app.get('/register', (req, res) => {
    res.render('register');
});

app.get('/post-ad', (req, res) => {
    res.render('post-ad');
});

app.get('/product/:id', (req, res) => {
    res.render('product');
});

// 5. Khởi động Server
app.listen(PORT, () => {
    console.log(`Server đang chạy tại: http://localhost:${PORT}`);
});