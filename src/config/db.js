require('dotenv').config();
const sql = require('mssql'); // Quay lại thư viện chuẩn

const config = {
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    server: process.env.DB_SERVER,
    database: process.env.DB_DATABASE,
    options: {
        encrypt: false,
        trustServerCertificate: true
    }
};

const poolPromise = new sql.ConnectionPool(config)
    .connect()
    .then(pool => {
        console.log(`✅ KẾT NỐI SQL SERVER THÀNH CÔNG (Tài khoản: ${config.user})!`);
        return pool;
    })
    .catch(err => {
        console.error('❌ Kết nối thất bại!', err.message);
    });

module.exports = { sql, poolPromise };