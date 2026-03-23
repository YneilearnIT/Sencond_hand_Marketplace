# C2C Second-hand Marketplace
 Nền tảng thương mại điện tử kết nối cá nhân để mua bán đồ cũ.

## Các tính năng chính (theo SRS):
* ***Người mua**: Tìm kiếm theo vị trí (GPS), lọc sản phẩm, nhắn tin thương lượng.
* ***Người bán**: Đăng tin nhanh, quản lý sản phẩm, mua gói đẩy tin.
* ***Admin**: Kiểm duyệt tin đăng và xử lý báo cáo vi phạm.

## Quy tắc nghiệp vụ (Business Rules):
* *Tin đăng giới hạn từ 1-6 hình ảnh.
* *Giá bán phải > 0 và làm tròn đến hàng nghìn.

# SHM - Second-hand Marketplace

Dự án website thương mại điện tử C2C dành cho đồ cũ, được xây dựng bằng Node.js, Express, Handlebars và PostgreSQL.

## 🛠 Yêu cầu hệ thống
* **Node.js**: Phiên bản 18.x trở lên.
* **PostgreSQL**: Phiên bản 14.x trở lên.
* **pgAdmin 4**: Để quản lý cơ sở dữ liệu.

## 🚀 Hướng dẫn cài đặt

### 1. Tải mã nguồn
```bash
git clone <link-github-cua-ban>
cd WEB_Analys




db.js

const pool = new Pool({
    user: 'postgres',
    host: '127.0.0.1',
    database: 'shm_market',
    password: 'MAT_Khau_cua_ban',
    port: 5432,
});