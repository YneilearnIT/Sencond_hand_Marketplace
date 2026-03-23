package com.yen.sencond_handmarketplace

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.ArrayList

class PostingActivity : AppCompatActivity() {

    private lateinit var btnAddImage: LinearLayout
    // Khai báo danh sách chứa 5 ô hình ảnh
    private lateinit var imageViews: List<ImageView>

    private lateinit var edtTitle: EditText
    private lateinit var edtDescription: EditText
    private lateinit var edtPrice: EditText
    private lateinit var edtCondition: EditText
    private lateinit var edtAddress: EditText
    private lateinit var btnConfirm: Button

    private lateinit var btnCate1: Button
    private lateinit var btnCate2: Button
    private lateinit var btnCate3: Button
    private lateinit var btnCate4: Button

    private var selectedCategoryName: String = ""
    private val imgbbApiKey = "7dbf488230ca76945d9bff93c50fad28"

    private val selectedBitmaps = mutableListOf<Bitmap>()
    private val uploadedUrls = ArrayList<String>()
    private var currentUploadIndex = 0

    // CHỌN NHIỀU ẢNH VÀ HIỂN THỊ LÊN TỪNG Ô
    private val pickMultipleImagesLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedBitmaps.clear()

            // Chỉ lấy tối đa 5 ảnh dù người dùng có chọn nhiều hơn
            val takeCount = minOf(uris.size, 5)

            for (i in 0 until takeCount) {
                val bitmap = if (Build.VERSION.SDK_INT >= 28) {
                    val source = android.graphics.ImageDecoder.createSource(this.contentResolver, uris[i])
                    android.graphics.ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(this.contentResolver, uris[i])
                }
                selectedBitmaps.add(bitmap)
            }

            // Đổ hình ảnh vào các ô UI tương ứng
            for (i in imageViews.indices) {
                if (i < selectedBitmaps.size) {
                    imageViews[i].setImageBitmap(selectedBitmaps[i])
                    imageViews[i].visibility = View.VISIBLE
                } else {
                    imageViews[i].visibility = View.GONE // Ẩn ô dư thừa
                }
            }

            Toast.makeText(this, "Đã chọn ${selectedBitmaps.size} ảnh!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_listing)

        btnAddImage = findViewById(R.id.btnAddImage)

        // Ánh xạ 5 ô ảnh
        val img1 = findViewById<ImageView>(R.id.imgPreview1)
        val img2 = findViewById<ImageView>(R.id.imgPreview2)
        val img3 = findViewById<ImageView>(R.id.imgPreview3)
        val img4 = findViewById<ImageView>(R.id.imgPreview4)
        val img5 = findViewById<ImageView>(R.id.imgPreview5)
        imageViews = listOf(img1, img2, img3, img4, img5)

        edtTitle = findViewById(R.id.edtTitle)
        edtDescription = findViewById(R.id.edtDescription)
        edtPrice = findViewById(R.id.edtPrice)
        edtCondition = findViewById(R.id.edtCondition)
        edtAddress = findViewById(R.id.edtAddress)
        btnConfirm = findViewById(R.id.btnConfirm)

        btnCate1 = findViewById(R.id.btnCate1)
        btnCate2 = findViewById(R.id.btnCate2)
        btnCate3 = findViewById(R.id.btnCate3)
        btnCate4 = findViewById(R.id.btnCate4)

        btnCate1.setOnClickListener { selectCategory(btnCate1, "Quần áo") }
        btnCate2.setOnClickListener { selectCategory(btnCate2, "Giày dép") }
        btnCate3.setOnClickListener { selectCategory(btnCate3, "Điện tử") }
        btnCate4.setOnClickListener { selectCategory(btnCate4, "Đồ gia dụng") }

        btnAddImage.setOnClickListener {
            pickMultipleImagesLauncher.launch("image/*")
        }

        btnConfirm.setOnClickListener {
            val title = edtTitle.text.toString().trim()
            val priceStr = edtPrice.text.toString().trim()
            val conditionStr = edtCondition.text.toString().trim()

            if (title.isEmpty() || priceStr.isEmpty() || conditionStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập thông tin bắt buộc (*)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedCategoryName.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn 1 danh mục!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedBitmaps.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 hình ảnh!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Bắt đầu upload chuỗi ảnh
            startUploadingProcess()
        }
    }

    private fun selectCategory(selectedBtn: Button, categoryName: String) {
        val allButtons = listOf(btnCate1, btnCate2, btnCate3, btnCate4)
        for (btn in allButtons) {
            btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E0E0E0"))
            btn.setTextColor(Color.BLACK)
        }
        selectedBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2196F3"))
        selectedBtn.setTextColor(Color.WHITE)
        selectedCategoryName = categoryName
    }

    private fun startUploadingProcess() {
        btnConfirm.isEnabled = false
        uploadedUrls.clear()
        currentUploadIndex = 0
        Toast.makeText(this, "Đang tải ${selectedBitmaps.size} ảnh lên hệ thống...", Toast.LENGTH_SHORT).show()
        uploadSingleImageToImgbb(selectedBitmaps[currentUploadIndex])
    }

    private fun uploadSingleImageToImgbb(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val requestFile = baos.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", "image_$currentUploadIndex.jpg", requestFile)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.imgbb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ImgbbApi::class.java)

        api.uploadImage(imgbbApiKey, body).enqueue(object : Callback<ImgbbResponse> {
            override fun onResponse(call: Call<ImgbbResponse>, response: Response<ImgbbResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val imageUrl = response.body()!!.data.url
                    uploadedUrls.add(imageUrl)
                    currentUploadIndex++

                    if (currentUploadIndex < selectedBitmaps.size) {
                        uploadSingleImageToImgbb(selectedBitmaps[currentUploadIndex])
                    } else {
                        // KHI HOÀN TẤT ĐẨY DỮ LIỆU SANG DASHBOARD
                        btnConfirm.isEnabled = true
                        Toast.makeText(this@PostingActivity, "Đăng tin thành công!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@PostingActivity, DashboardActivity::class.java)
                        intent.putExtra("TITLE", edtTitle.text.toString().trim())
                        intent.putExtra("PRICE", edtPrice.text.toString().trim() + " VNĐ")
                        intent.putExtra("ADDRESS", edtAddress.text.toString().trim())
                        intent.putExtra("DESC", edtDescription.text.toString().trim())
                        intent.putStringArrayListExtra("IMAGES", uploadedUrls)

                        startActivity(intent)
                        finish()
                    }
                }
            }

            override fun onFailure(call: Call<ImgbbResponse>, t: Throwable) {
                btnConfirm.isEnabled = true
                Toast.makeText(this@PostingActivity, "Lỗi tải ảnh: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}