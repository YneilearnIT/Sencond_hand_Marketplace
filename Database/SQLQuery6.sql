CREATE TABLE notifications (
    noti_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NULL, -- Nếu NULL thì là gửi cho Admin, có ID thì gửi cho User
    message NVARCHAR(MAX),
    is_read BIT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE()
);