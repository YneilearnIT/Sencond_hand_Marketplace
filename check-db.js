const pool = require('./src/config/db'); // Đường dẫn đến file db.js của bạn

pool.query('SELECT NOW()', (err, res) => {
    if (err) {
        console.error('❌ KẾT NỐI THẤT BẠI:', err.message);
        console.log('Hệ thống gợi ý: Kiểm tra lại Mật khẩu hoặc tên Database trong file db.js');
    } else {
        console.log('✅ KẾT NỐI THÀNH CÔNG!');
        console.log('Thời gian hiện tại trong Database là:', res.rows[0].now);
    }
    pool.end(); // Đóng kết nối sau khi test
});