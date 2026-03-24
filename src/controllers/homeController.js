const { sql, poolPromise } = require('../config/db'); // Cập nhật cách import db

const getHomeListings = async (req, res) => {
    // Lưu ý: Cột lưu vị trí hiện tại của bạn là location_gps (chuỗi ký tự nvarchar)
    // Chức năng tính toán khoảng cách (khoảng cách Haversine) yêu cầu kiểu dữ liệu Float cho vĩ độ/kinh độ.
    // Tạm thời mình sẽ trả về danh sách 20 tin mới nhất và ưu tiên tin VIP. 
    // Nếu bạn muốn tính toán khoảng cách theo GPS thật, cần sửa bảng listings để tách riêng 2 cột Lat/Lon kiểu Float.

    try {
        const pool = await poolPromise;
        
        // Dùng TOP thay vì LIMIT, dùng is_vip thay vì promotion_expiry_date
        const query = `
            SELECT TOP 20 *
            FROM listings
            WHERE status = 'Active'
            ORDER BY 
                is_vip DESC, -- Ưu tiên tin VIP (1) lên đầu
                created_at DESC
        `;

        const result = await pool.request().query(query);
        res.status(200).json(result.recordset); // Dùng recordset thay vì rows
    } catch (err) {
        console.error(err);
        res.status(500).send("Lỗi lấy danh sách sản phẩm");
    }
};

module.exports = { getHomeListings };