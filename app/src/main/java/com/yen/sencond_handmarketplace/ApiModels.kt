package com.yen.sencond_handmarketplace

// Cấu trúc gói hàng gửi lên API của bạn
data class ProductRequest(
    val title: String,
    val price: String,
    val address: String,
    val description: String,
    val category: String,
    val images: List<String> // Danh sách link ảnh ImgBB
)

// Cấu trúc nhận phản hồi từ API (Thành công hay thất bại)
data class ApiResponse(
    val success: Boolean,
    val message: String
)