const express = require('express');
const { engine } = require('express-handlebars');
const path = require('path');

const app = express();

// Cấu hình Handlebars
app.engine('hbs', engine({ extname: '.hbs', defaultLayout: 'main' }));
app.set('view engine', 'hbs');
app.set('views', './views');

app.use(express.static('public'));
app.use(express.urlencoded({ extended: true }));

// Dữ liệu mẫu (Mock Data) dựa trên đặc tả 
const categories = [
    { id: 1, name: 'Điện thoại' }, { id: 2, name: 'Xe cộ' }, { id: 3, name: 'Đồ gia dụng' }
];

let listings = [
    { id: 1, title: 'iPhone 13 Pro Max', price: '15.000.000', location: 'Quận 7, HCM', condition: 95, img: 'https://via.placeholder.com/150' },
    { id: 2, title: 'Xe Honda Vision 2022', price: '28.500.000', location: 'Bình Thạnh, HCM', condition: 90, img: 'https://via.placeholder.com/150' }
];

// Routes
app.get('/', (req, res) => {
    res.render('home', { title: 'Trang chủ - SHM', listings });
});

app.get('/product/:id', (req, res) => {
    const item = listings.find(l => l.id == req.params.id);
    res.render('product', { item });
});

app.get('/post-ad', (req, res) => {
    res.render('post-ad', { categories });
});

app.post('/post-ad', (req, res) => {
    // Xử lý lưu tin đăng [cite: 98]
    const { title, price, categoryId, description } = req.body;
    console.log("Đang kiểm duyệt tin:", title); // Business Rule: Chờ duyệt [cite: 185]
    res.send('<h3>Tin đăng của bạn đang được kiểm duyệt! [cite: 238]</h3><a href="/">Về trang chủ</a>');
});

app.listen(3000, () => console.log('Server chạy tại http://localhost:3000'));