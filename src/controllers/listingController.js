const { sql, poolPromise } = require('../config/db'); // Cập nhật cách import db

const createListing = async (req, res) => {
    const { title, description, price, condition_percent, category_id, location_gps, seller_id } = req.body;

    // Kiểm tra giá bán: phải > 0 và làm tròn đến hàng nghìn (BR-09)
    if (price <= 0 || price % 1000 !== 0) {
        return res.status(400).send("Giá bán không hợp lệ (phải > 0 và chia hết cho 1000)");
    }

    try {
        const pool = await poolPromise;

        // Kiểm tra chống Spam: tối đa 5 tin/ngày (BR-06)
        // Dùng seller_id thay vì user_id theo chuẩn database
        const spamCheckQuery = `
            SELECT COUNT(*) AS count 
            FROM listings 
            WHERE seller_id = @seller_id AND CAST(created_at AS DATE) = CAST(GETDATE() AS DATE)
        `;
        
        const spamCheck = await pool.request()
            .input('seller_id', sql.Int, seller_id)
            .query(spamCheckQuery);

        if (parseInt(spamCheck.recordset[0].count) >= 5) {
            return res.status(400).send("Bạn đã đạt giới hạn đăng 5 tin mỗi ngày");
        }

        // Lưu tin đăng mới với trạng thái Chờ duyệt (Pending)
        // Dùng OUTPUT INSERTED.* thay cho RETURNING *
        const insertQuery = `
            INSERT INTO listings (seller_id, category_id, title, description, price, condition_percentage, location_gps, status)
            OUTPUT INSERTED.*
            VALUES (@seller_id, @category_id, @title, @description, @price, @condition_percent, @location_gps, 'Pending');
        `;
        
        const result = await pool.request()
            .input('seller_id', sql.Int, seller_id)
            .input('category_id', sql.Int, category_id)
            .input('title', sql.NVarChar, title)
            .input('description', sql.NVarChar, description)
            .input('price', sql.Decimal(18,2), price)
            .input('condition_percent', sql.Int, condition_percent)
            .input('location_gps', sql.NVarChar, location_gps || null) // Database cho phép null cột này
            .query(insertQuery);

        res.status(201).json({ 
            message: "Đăng tin thành công, vui lòng chờ duyệt!", 
            data: result.recordset[0] 
        });
    } catch (err) {
        console.error(err);
        res.status(500).send("Lỗi hệ thống khi đăng tin");
    }
};

module.exports = { createListing };