const { sql, poolPromise } = require('./src/config/db');

async function testConnection() {
    try {
        const pool = await poolPromise;
        const result = await pool.request().query('SELECT GETDATE() AS now');
        console.log('Thời gian hiện tại trong Database là:', result.recordset[0].now);
        process.exit(0);
    } catch (err) {
        console.error('Hệ thống gợi ý: Kiểm tra lại tài khoản, mật khẩu hoặc tên Server trong file .env');
        process.exit(1);
    }
}

testConnection();