const sql = require('mssql');
require('dotenv').config();

const config = {
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    server: process.env.DB_SERVER, 
    database: process.env.DB_NAME,
    options: {
        encrypt: false, // Phải để false khi chạy ở localhost
        trustServerCertificate: true 
    }
};

// Khởi tạo kết nối dạng Pool để tái sử dụng
const poolPromise = new sql.ConnectionPool(config)
    .connect()
    .then(pool => {
        console.log('✅ KẾT NỐI SQL SERVER THÀNH CÔNG!');
        return pool;
    })
    .catch(err => {
        console.error('❌ KẾT NỐI SQL SERVER THẤT BẠI:', err.message);
        process.exit(-1);
    });

module.exports = { sql, poolPromise };