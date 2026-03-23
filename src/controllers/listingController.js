const pool = require('../config/db');

const createListing = async (req, res) => {
    const { title, description, price, condition_percent, category_id, latitude, longitude, user_id } = req.body;

    // Kiểm tra giá bán: phải > 0 và làm tròn đến hàng nghìn (BR-09)
    if (price <= 0 || price % 1000 !== 0) {
        return res.status(400).send("Giá bán không hợp lệ (phải > 0 và chia hết cho 1000)");
    }

    try {
        // Kiểm tra chống Spam: tối đa 5 tin/ngày (BR-06)
        const spamCheck = await pool.query(
            "SELECT COUNT(*) FROM Listings WHERE user_id = $1 AND created_at > CURRENT_DATE",
            [user_id]
        );

        if (parseInt(spamCheck.rows[0].count) >= 5) {
            return res.status(400).send("Bạn đã đạt giới hạn đăng 5 tin mỗi ngày");
        }

        // Lưu tin đăng mới với trạng thái Chờ duyệt (Pending)
        const query = `
            INSERT INTO Listings (user_id, category_id, title, description, price, condition_percent, latitude, longitude, status)
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, 'Pending')
            RETURNING *;
        `;
        
        const result = await pool.query(query, [user_id, category_id, title, description, price, condition_percent, latitude, longitude]);
        res.status(201).json({ message: "Đăng tin thành công, vui lòng chờ duyệt!", data: result.rows[0] });
    } catch (err) {
        console.error(err);
        res.status(500).send("Lỗi hệ thống khi đăng tin");
    }
};

module.exports = { createListing };