package com.yen.sencond_handmarketplace

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MyBackendApi {
    // Sửa chữ "api/products" thành đúng cái đường dẫn API Backend của bạn nhé
    @POST("api/products")
    fun createNewProduct(@Body product: ProductRequest): Call<ApiResponse>
}