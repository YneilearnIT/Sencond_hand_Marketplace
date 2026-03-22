package com.yen.sencond_handmarketplace

data class ImgbbResponse(
    val data: ImgbbData,
    val success: Boolean,
    val status: Int
)

data class ImgbbData(
    val url: String
)