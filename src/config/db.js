require('dotenv').config();
const sql = require('mssql');

const config = {
    user: process.env.DB_USER || 'sa', // Tài khoản đăng nhập SSMS (thường là sa)
    password: process.env.DB_PASSWORD || '2005', // Mật khẩu của bạn
    server: process.env.DB_SERVER || 'localhost', 
    database: process.env.DB_DATABASE || 'C2C',
    options: {
        encrypt: false, // Để false nếu chạy localhost
        trustServerCertificate: true // Quan trọng để tránh lỗi chứng chỉ trên máy cá nhân
    }
};

const poolPromise = new sql.ConnectionPool(config)
    .connect()
    .then(pool => {
        console.log('✅ Đã kết nối thành công với SQL Server (SSMS)');
        return pool;
    })
    .catch(err => {
        console.error('❌ Kết nối SQL Server thất bại!', err);
        process.exit(1);
    });

module.exports = {
    sql,
    query: async (text, params) => {
        const pool = await poolPromise;
        const request = pool.request();
        
        // Chuyển đổi tham số từ kiểu $1, $2 sang kiểu của mssql nếu cần
        if (params) {
            params.forEach((val, index) => {
                request.input(`param${index + 1}`, val);
                text = text.replace(`$${index + 1}`, `@param${index + 1}`);
            });
        }
        return request.query(text);
    }
};