const pool = require('../config/db');

const getHomeListings = async (req, res) => {
    const { lat, lon } = req.query;
    // Mặc định tọa độ HCM nếu người dùng không cung cấp GPS
    const userLat = lat || 10.762622;
    const userLon = lon || 106.660172;

    try {
        const query = `
            SELECT *,
            (6371 * acos(cos(radians($1)) * cos(radians(latitude)) * cos(radians(longitude) - radians($2)) + sin(radians($1)) * sin(radians(latitude)))) AS distance
            FROM Listings
            WHERE status = 'Active'
            ORDER BY 
                CASE WHEN promotion_expiry_date > CURRENT_TIMESTAMP THEN 0 ELSE 1 END, -- Ưu tiên tin đẩy lên đầu
                distance ASC, -- Ưu tiên khoảng cách gần nhất
                created_at DESC
            LIMIT 20; -- Áp dụng phân trang để tối ưu hiệu suất
        `;

        const result = await pool.query(query, [userLat, userLon]);
        res.status(200).json(result.rows);
    } catch (err) {
        console.error(err);
        res.status(500).send("Lỗi lấy danh sách sản phẩm");
    }
};

module.exports = { getHomeListings };