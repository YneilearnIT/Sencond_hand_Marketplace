package com.yen.sencond_handmarketplace // Dòng này giúp các file nhận ra nhau (nhớ giữ nguyên nếu tên package của bạn y hệt vậy)

// Import thư viện đầy đủ để không bị báo đỏ
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// 1. Gói dữ liệu gửi đi
data class ProductRequest(
    val title: String,
    val price: String,
    val address: String,
    val description: String,
    val category: String,
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