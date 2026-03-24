package com.yen.sencond_handmarketplace

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ImgbbApi {
    @Multipart
    @POST("1/upload")
    fun uploadImage(
        @Query("key") apiKey: String,
        @Part image: MultipartBody.Part
    ): Call<ImgbbResponse>
}