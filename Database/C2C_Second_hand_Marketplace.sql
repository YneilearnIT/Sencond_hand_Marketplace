CREATE DATABASE C2C;
GO

USE C2C;;
GO

-- 1. Bảng Danh mục sản phẩm (Ví dụ: Điện tử, Thời trang, Đồ gia dụng...)
CREATE TABLE categories (
    category_id int IDENTITY(1,1),
    category_name nvarchar(100) NOT NULL,
    description nvarchar(max),
    icon_url varchar(255),
    PRIMARY KEY (category_id)
);

-- 2. Bảng Người dùng (Tích hợp cả vai trò Người mua và Người bán)
CREATE TABLE users (
    user_id int IDENTITY(1,1),
    phone_number varchar(15) NOT NULL UNIQUE,
    password_hash varchar(255) NOT NULL,
    full_name nvarchar(100) NOT NULL,
    email varchar(100),
    address nvarchar(255),
    id_card_number varchar(20), -- Dùng cho xác thực tài khoản
    reputation_score decimal(3,2) DEFAULT 5.0, -- Điểm uy tín
    status nvarchar(20) DEFAULT 'Active', -- Active, Blocked
    created_at datetime DEFAULT GETDATE(),
    PRIMARY KEY (user_id)
);

-- 3. Bảng Tin đăng sản phẩm 
CREATE TABLE listings (
    listing_id int IDENTITY(1,1),
    seller_id int NOT NULL,
    category_id int NOT NULL,
    title nvarchar(255) NOT NULL,
    description nvarchar(max) NOT NULL,
    price decimal(18,2) NOT NULL,
    condition_percentage int NOT NULL, -- Độ mới sản phẩm (%)
    location_gps nvarchar(100), -- Vị trí giao dịch
    status nvarchar(20) DEFAULT 'Pending', -- Pending, Active, Sold, Hidden, Rejected
    is_vip bit DEFAULT 0, -- Đánh dấu tin được đẩy (VIP)
    created_at datetime DEFAULT GETDATE(),
    PRIMARY KEY (listing_id),
    FOREIGN KEY (seller_id) REFERENCES users (user_id),
    FOREIGN KEY (category_id) REFERENCES categories (category_id)
);

-- 4. Bảng Hình ảnh sản phẩm (Một tin đăng có nhiều ảnh)
CREATE TABLE listing_images (
    image_id int IDENTITY(1,1),
    listing_id int NOT NULL,
    image_url varchar(255) NOT NULL,
    is_thumbnail bit DEFAULT 0,
    PRIMARY KEY (image_id),
    FOREIGN KEY (listing_id) REFERENCES listings (listing_id) ON DELETE CASCADE
);

-- 5. Bảng Cuộc hội thoại (Chat)
CREATE TABLE conversations (
    conversation_id int IDENTITY(1,1),
    listing_id int NOT NULL,
    buyer_id int NOT NULL,
    seller_id int NOT NULL,
    created_at datetime DEFAULT GETDATE(),
    PRIMARY KEY (conversation_id),
    FOREIGN KEY (listing_id) REFERENCES listings (listing_id),
    FOREIGN KEY (buyer_id) REFERENCES users (user_id),
    FOREIGN KEY (seller_id) REFERENCES users (user_id)
);

-- 6. Bảng Chi tiết tin nhắn
CREATE TABLE messages (
    message_id int IDENTITY(1,1),
    conversation_id int NOT NULL,
    sender_id int NOT NULL,
    content nvarchar(max),
    is_read bit DEFAULT 0,
    sent_at datetime DEFAULT GETDATE(),
    PRIMARY KEY (message_id),
    FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id),
    FOREIGN KEY (sender_id) REFERENCES users (user_id)
);

-- 7. Bảng Gói dịch vụ đẩy tin (VIP)
CREATE TABLE boost_packages (
    package_id int IDENTITY(1,1),
    package_name nvarchar(50) NOT NULL,
    price decimal(10,2) NOT NULL,
    duration_hours int NOT NULL, -- Thời gian đẩy tin (VD: 24h)
    PRIMARY KEY (package_id)
);

-- 8. Bảng Giao dịch mua gói đẩy tin (Thanh toán)
CREATE TABLE payments (
    payment_id int IDENTITY(1,1),
    user_id int NOT NULL,
    listing_id int NOT NULL,
    package_id int NOT NULL,
    amount decimal(10,2) NOT NULL,
    payment_method nvarchar(50), -- MoMo, VNPay...
    status nvarchar(20), -- Success, Pending, Failed
    expiry_date datetime, -- Ngày hết hạn VIP
    payment_date datetime DEFAULT GETDATE(),
    PRIMARY KEY (payment_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (listing_id) REFERENCES listings (listing_id),
    FOREIGN KEY (package_id) REFERENCES boost_packages (package_id)
);

-- 9. Bảng Báo cáo vi phạm (Dành cho Admin xử lý)
CREATE TABLE reports (
    report_id int IDENTITY(1,1),
    reporter_id int NOT NULL,
    target_listing_id int,
    target_user_id int,
    reason nvarchar(max) NOT NULL,
    status nvarchar(20) DEFAULT 'Processing', -- Processing, Resolved, Dismissed
    created_at datetime DEFAULT GETDATE(),
    PRIMARY KEY (report_id),
    FOREIGN KEY (reporter_id) REFERENCES users (user_id),
    FOREIGN KEY (target_listing_id) REFERENCES listings (listing_id)
);

INSERT INTO categories (category_name, description) VALUES 
(N'Quần áo', N'Áo thun, sơ mi, quần jean, áo khoác'),
(N'Giày dép', N'Giày thể thao, giày tây, dép thời trang'),
(N'Đồ điện tử', N'Điện thoại, Laptop, loa, tai nghe'),
(N'Đồ gia dụng', N'Nồi cơm, máy xay sinh tố, đồ dùng bếp');


INSERT INTO users (phone_number, password_hash, full_name, email, address, reputation_score) VALUES 
('0901234567', 'hash_pass_1', N'Nguyễn Văn An', 'an@gmail.com', N'Quận 1, HCM', 5.0),
('0907654321', 'hash_pass_2', N'Trần Thị Bình', 'binh@gmail.com', N'Quận 7, HCM', 4.8),
('0911223344', 'hash_pass_3', N'Lê Minh Cường', 'cuong@gmail.com', N'Hoàn Kiếm, HN', 4.5),
('0988776655', 'hash_pass_4', N'Phạm Mỹ Hạnh', 'hanh@gmail.com', N'Đống Đa, HN', 5.0),
('0933445566', 'hash_pass_5', N'Đỗ Hoàng Nam', 'nam@gmail.com', N'Hải Châu, Đà Nẵng', 3.2);

INSERT INTO boost_packages (package_name, price, duration_hours) VALUES 
(N'Gói Đồng', 20000, 24),
(N'Gói Bạc', 50000, 72),
(N'Gói Vàng', 100000, 168);


INSERT INTO listings (seller_id, category_id, title, description, price, condition_percentage, location_gps, status, is_vip) VALUES 
(1, 1, N'Áo khoác Bomber', N'Mới 99%', 450000, 99, N'HCM', 'Active', 0),
(2, 1, N'Quần Jean Levi''s', N'Size 32', 800000, 95, N'Hà Nội', 'Active', 0),
(3, 2, N'Giày Nike AF1', N'Size 42', 1200000, 98, N'Đà Nẵng', 'Active', 0),
(4, 2, N'Dép quai ngang', N'Bền', 50000, 100, N'HCM', 'Active', 0),
(5, 3, N'Tai nghe Sony', N'Chống ồn', 4500000, 95, N'Hà Nội', 'Active', 1), -- Tin VIP
(1, 3, N'Chuột Logitech', N'Gaming', 600000, 85, N'HCM', 'Active', 0),
(2, 4, N'Máy xay Philips', N'Xay đá tốt', 700000, 90, N'Hà Nội', 'Active', 0),
(3, 4, N'Ấm siêu tốc', N'1.8 Lít', 300000, 95, N'Đà Nẵng', 'Active', 0),
(4, 3, N'Loa Bluetooth Go 3', N'Âm hay', 800000, 99, N'HCM', 'Pending', 0),
(5, 1, N'Áo thun Local Brand', N'Size M', 200000, 90, N'Hà Nội', 'Sold', 0); -- Tin đã bán

INSERT INTO listing_images (listing_id, image_url, is_thumbnail) VALUES 
(1, 'bomber.jpg', 1), (2, 'levis.jpg', 1), (3, 'nike.jpg', 1),
(4, 'dep.jpg', 1), (5, 'sony.jpg', 1), (6, 'logi.jpg', 1),
(7, 'philips.jpg', 1), (8, 'am.jpg', 1), (9, 'go3.jpg', 1), (10, 'shirt.jpg', 1);

INSERT INTO boost_packages (package_name, price, duration_hours) VALUES 
(N'Gói Đồng', 20000, 24), (N'Gói Bạc', 50000, 72), (N'Gói Vàng', 100000, 168);

INSERT INTO payments (user_id, listing_id, package_id, amount, payment_method, status, payment_date) VALUES 
(5, 5, 3, 100000, N'Chuyển khoản', 'Success', GETDATE());

-- Tạo tài khoản đăng nhập hệ thống
CREATE LOGIN Admin_SanDoCu WITH PASSWORD = '123456@';
CREATE LOGIN User_Seller01 WITH PASSWORD = 'UserPassword123!';
CREATE LOGIN User_Buyer01 WITH PASSWORD = 'UserPassword123!';

-- Tạo User cho Database
CREATE USER AdminUser FOR LOGIN Admin_SanDoCu;
CREATE USER Seller01 FOR LOGIN User_Seller01;
CREATE USER Buyer01 FOR LOGIN User_Buyer01;

-- Tạo các nhóm vai trò (Roles)
CREATE ROLE Role_Admin;
CREATE ROLE Role_Seller;
CREATE ROLE Role_Buyer;

-- Thêm User vào nhóm
ALTER ROLE Role_Admin ADD MEMBER AdminUser;
ALTER ROLE Role_Seller ADD MEMBER Seller01;
ALTER ROLE Role_Buyer ADD MEMBER Buyer01;

-- 1. Nhóm Admin: Toàn quyền trên các bảng (Users, Categories, Reports)
GRANT CONTROL TO AdminUser; 

-- 2. Nhóm Buyer: Chỉ được Xem tin đăng, được Insert vào bảng Reports (Báo cáo vi phạm)
GRANT SELECT ON listings TO Role_Buyer;
GRANT SELECT ON listing_images TO Role_Buyer;
GRANT INSERT ON reports TO Role_Buyer;

-- 3. Nhóm Seller: Được phép đăng tin (Insert) và sửa tin của mình
GRANT INSERT, UPDATE, DELETE ON listings TO Role_Seller;
GRANT INSERT ON listing_images TO Role_Seller;

-- 4. Bảo mật cấp cột (Object Level): Chỉ cho phép User xem tên người bán, không cho xem số CCCD
GRANT SELECT ON users(full_name, reputation_score) TO Role_Buyer;
-- Cấm xem cột nhạy cảm
DENY SELECT ON users(id_card_number, password_hash) TO Role_Buyer;
DENY SELECT ON users(id_card_number, password_hash) TO Role_Seller;

-- 5.  Chat (Giao tiếp) Cả Người mua và Người bán đều cần quyền Xem, Thêm cuộc hội thoại và gửi tin nhắn
GRANT SELECT, INSERT ON conversations TO Role_Buyer;
GRANT SELECT, INSERT ON conversations TO Role_Seller;

GRANT SELECT, INSERT ON messages TO Role_Buyer;
GRANT SELECT, INSERT ON messages TO Role_Seller;

-- 6.  Mua gói đẩy tin (Thanh toán) Người dùng cần xem được danh sách các gói (boost_packages) và được phép tạo giao dịch (payments)
GRANT SELECT ON boost_packages TO Role_Buyer;
GRANT SELECT ON boost_packages TO Role_Seller;

GRANT INSERT, SELECT ON payments TO Role_Buyer;
GRANT INSERT, SELECT ON payments TO Role_Seller;

-- 7. Quyền xem hình ảnh sản phẩm 
GRANT SELECT ON listing_images TO Role_Buyer;
GRANT SELECT ON listing_images TO Role_Seller;
USE C2C;
GO

----------------------------------------------------------------
-- 1. HỆ THỐNG VIEW (TRUY VẤN DỮ LIỆU)
----------------------------------------------------------------

-- V1. View "Quản lý kho hàng cá nhân": Dùng cho màn hình "Tin đăng của tôi"
CREATE OR ALTER VIEW v_My_Inventory AS
SELECT 
    L.seller_id,
    L.listing_id,
    L.title,
    L.price,
    L.status,
    L.is_vip,
    L.created_at,
    (SELECT COUNT(*) FROM reports WHERE target_listing_id = L.listing_id) AS report_count,
    CASE 
        WHEN L.is_vip = 1 THEN N'Đang đẩy tin'
        ELSE N'Tin thường'
    END AS display_type
FROM listings L;
GO

-- V2. View "Lịch sử mua gói VIP": Dành cho Dashboard Admin theo dõi doanh thu
CREATE OR ALTER VIEW v_Revenue_Details AS
SELECT 
    P.payment_id,
    U.full_name AS customer_name,
    L.title AS product_name,
    B.package_name,
    P.amount,
    P.payment_method,
    P.payment_date,
    P.status AS payment_status
FROM payments P
JOIN users U ON P.user_id = U.user_id
JOIN listings L ON P.listing_id = L.listing_id
JOIN boost_packages B ON P.package_id = B.package_id;
GO

----------------------------------------------------------------
-- 2. HỆ THỐNG STORED PROCEDURE (XỬ LÝ NGHIỆP VỤ)
----------------------------------------------------------------

-- SP1. Đăng tin mới kèm ảnh (Xử lý đồng thời 2 bảng Listings và Images)
CREATE OR ALTER PROCEDURE sp_CreateListing
    @SellerID int,
    @CategoryID int,
    @Title nvarchar(255),
    @Description nvarchar(max),
    @Price decimal(18,2),
    @Condition int,
    @Location nvarchar(100),
    @ImageUrl varchar(255)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        -- Thêm vào bảng Listings
        INSERT INTO listings (seller_id, category_id, title, description, price, condition_percentage, location_gps, status)
        VALUES (@SellerID, @CategoryID, @Title, @Description, @Price, @Condition, @Location, 'Pending');

        -- Lấy ID của tin vừa tạo
        DECLARE @NewListingID int = SCOPE_IDENTITY();

        -- Thêm ảnh đại diện vào bảng Listing_Images
        INSERT INTO listing_images (listing_id, image_url, is_thumbnail)
        VALUES (@NewListingID, @ImageUrl, 1);

        COMMIT TRANSACTION;
        SELECT @NewListingID AS NewID, N'Thành công' AS Message;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SELECT 0 AS NewID, ERROR_MESSAGE() AS Message;
    END CATCH
END;
GO

-- SP2. Xử lý thanh toán và nâng cấp tin VIP
CREATE OR ALTER PROCEDURE sp_ProcessPayment
    @UserID int,
    @ListingID int,
    @PackageID int,
    @Amount decimal(10,2),
    @Method nvarchar(50)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        -- Ghi nhận lịch sử thanh toán
        INSERT INTO payments (user_id, listing_id, package_id, amount, payment_method, status, payment_date, expiry_date)
        VALUES (@UserID, @ListingID, @PackageID, @Amount, @Method, 'Success', GETDATE(), DATEADD(day, 3, GETDATE())); 

        -- Cập nhật trạng thái VIP cho tin đăng
        UPDATE listings SET is_vip = 1 WHERE listing_id = @ListingID;

        COMMIT TRANSACTION;
        PRINT N'Thanh toán thành công và kích hoạt tin VIP!';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        PRINT N'Lỗi thanh toán: ' + ERROR_MESSAGE();
    END CATCH
END;
GO

----------------------------------------------------------------
-- 3. HỆ THỐNG TRIGGER (TỰ ĐỘNG HÓA & BẢO MẬT)
----------------------------------------------------------------

-- T1. Tự động ẩn tin khi bị báo cáo vi phạm quá 5 lần
CREATE OR ALTER TRIGGER trg_AutoHideBadListing
ON reports
AFTER INSERT
AS
BEGIN
    DECLARE @ListingID int;
    SELECT @ListingID = target_listing_id FROM inserted;

    IF (SELECT COUNT(*) FROM reports WHERE target_listing_id = @ListingID) >= 5
    BEGIN
        UPDATE listings SET status = 'Hidden' WHERE listing_id = @ListingID;
        PRINT N'Hệ thống: Tin đăng ID ' + CAST(@ListingID AS varchar) + N' đã bị ẩn tự động.';
    END
END;
GO

-- T2. Chặn chỉnh sửa tin đăng đã ở trạng thái 'Đã bán'
CREATE OR ALTER TRIGGER trg_PreventUpdateOnSoldListing
ON listings
AFTER UPDATE
AS
BEGIN
    -- Kiểm tra nếu trạng thái cũ là 'Sold' thì không cho sửa bất cứ thông tin gì
    IF EXISTS (SELECT 1 FROM deleted WHERE status = 'Sold')
    BEGIN
        RAISERROR (N'Không thể chỉnh sửa tin đăng đã ở trạng thái Đã bán!', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO

-- T3. Tự động dọn dẹp ảnh khi xóa tin đăng
CREATE OR ALTER TRIGGER trg_CleanUpImagesOnDelete
ON listings
INSTEAD OF DELETE
AS
BEGIN
    DECLARE @ID int;
    SELECT @ID = listing_id FROM deleted;

    -- Xóa ảnh trước để tránh lỗi ràng buộc khóa ngoại
    DELETE FROM listing_images WHERE listing_id = @ID;
    -- Sau đó xóa tin đăng
    DELETE FROM listings WHERE listing_id = @ID;
    
    PRINT N'Đã dọn dẹp dữ liệu ảnh cho tin đăng bị xóa.';
END;
GO

----------------------------------------------------------------
-- 4. CÁC CÂU LỆNH KIỂM TRA (TEST SCRIPTS)
----------------------------------------------------------------
/*
-- Kiểm tra View
SELECT * FROM v_My_Inventory;
SELECT * FROM v_Revenue_Details;

-- Thử đăng tin mới qua SP
EXEC sp_CreateListing @SellerID = 1, @CategoryID = 1, @Title = N'Sản phẩm Test', 
     @Description = N'Mô tả', @Price = 100000, @Condition = 95, @Location = N'HCM', @ImageUrl = 'test.jpg';

-- Thử xóa tin để xem Trigger CleanUp chạy
DELETE FROM listings WHERE listing_id = [ID_VUA_TAO];
*/