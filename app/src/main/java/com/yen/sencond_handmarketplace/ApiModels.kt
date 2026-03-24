<<<<<<< HEAD
package com.yen.sencond_handmarketplace // Dòng này giúp các file nhận ra nhau (nhớ giữ nguyên nếu tên package của bạn y hệt vậy)

// Import thư viện đầy đủ để không bị báo đỏ
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// 1. Gói dữ liệu gửi đi
=======
package com.yen.sencond_handmarketplace

// Cấu trúc gói hàng gửi lên API của bạn
>>>>>>> 7ec05b6a5ccc8b019cca11c35ccd913321cfd47f
data class ProductRequest(
    val title: String,
    val price: String,
    val address: String,
    val description: String,
    val category: String,
<<<<<<< HEAD
    val images: List<String>
)

// 2. Gói dữ liệu nhận về
data class ApiResponse(
    val success: Boolean,
    val message: String
)

// 3. Cầu nối API
interface MyBackendApi {
    @POST("http://10.0.2.2:3000/")
    fun createNewProduct(@Body product: ProductRequest): Call<ApiResponse>
}
=======
    val images: List<String> // Danh sách link ảnh ImgBB
)

// Cấu trúc nhận phản hồi từ API (Thành công hay thất bại)
data class ApiResponse(
    val success: Boolean,
    val message: String
)
>>>>>>> 7ec05b6a5ccc8b019cca11c35ccd913321cfd47f
