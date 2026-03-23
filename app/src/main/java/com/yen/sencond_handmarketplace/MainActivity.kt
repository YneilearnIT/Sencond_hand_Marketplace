package com.yen.sencond_handmarketplace

import android.content.Intent // THÊM DÒNG NÀY ĐỂ DÙNG LỆNH CHUYỂN TRANG
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

class MainActivity : AppCompatActivity() {

    private lateinit var btnAddImage: LinearLayout
    private lateinit var imgPreview: ImageView
    private lateinit var edtTitle: EditText
    private lateinit var edtDescription: EditText
    private lateinit var edtPrice: EditText
    private lateinit var edtCondition: EditText
    private lateinit var edtAddress: EditText
    private lateinit var btnConfirm: Button

    // Khai báo 4 nút danh mục
    private lateinit var btnCate1: Button
    private lateinit var btnCate2: Button
    private lateinit var btnCate3: Button
    private lateinit var btnCate4: Button

    // Biến lưu trữ danh mục đang được chọn
    private var selectedCategoryName: String = ""

    private val imgbbApiKey = "7dbf488230ca76945d9bff93c50fad28"
    private var selectedImageBitmap: Bitmap? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageBitmap = if (Build.VERSION.SDK_INT >= 28) {
                val source = android.graphics.ImageDecoder.createSource(this.contentResolver, uri)
                android.graphics.ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            }
            imgPreview.setImageBitmap(selectedImageBitmap)
            imgPreview.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_listing)

        btnAddImage = findViewById(R.id.btnAddImage)
        imgPreview = findViewById(R.id.imgPreview)
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

        // Cài đặt sự kiện bấm cho từng nút Danh mục
        btnCate1.setOnClickListener { selectCategory(btnCate1, "Quần áo") }
        btnCate2.setOnClickListener { selectCategory(btnCate2, "Giày dép") }
        btnCate3.setOnClickListener { selectCategory(btnCate3, "Điện tử") }
        btnCate4.setOnClickListener { selectCategory(btnCate4, "Đồ gia dụng") }

        btnAddImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnConfirm.setOnClickListener {
            val title = edtTitle.text.toString().trim()
            val priceStr = edtPrice.text.toString().trim()
            val conditionStr = edtCondition.text.toString().trim()

            if (title.isEmpty() || priceStr.isEmpty() || conditionStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ các thông tin bắt buộc (*)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ràng buộc phải chọn danh mục
            if (selectedCategoryName.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn 1 danh mục sản phẩm!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toLongOrNull() ?: 0L
            if (price <= 0 || price % 1000 != 0L) {
                Toast.makeText(this, "Giá bán phải lớn hơn 0 và làm tròn đến hàng nghìn (VD: 15000)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageBitmap == null) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 hình ảnh sản phẩm!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadImageToImgbb(selectedImageBitmap!!)
        }
    }

    // Hàm xử lý đổi màu nút khi chọn Danh mục
    private fun selectCategory(selectedBtn: Button, categoryName: String) {
        val allButtons = listOf(btnCate1, btnCate2, btnCate3, btnCate4)

        // Trả tất cả các nút về màu xám mặc định
        for (btn in allButtons) {
            btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E0E0E0"))
            btn.setTextColor(Color.BLACK)
        }

        // Tô màu xanh dương cho nút vừa được bấm
        selectedBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2196F3"))
        selectedBtn.setTextColor(Color.WHITE)

        // Lưu lại tên danh mục để gửi lên Server
        selectedCategoryName = categoryName
    }

    private fun uploadImageToImgbb(bitmap: Bitmap) {
        Toast.makeText(this, "Đang tải dữ liệu lên hệ thống...", Toast.LENGTH_SHORT).show()
        btnConfirm.isEnabled = false

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val requestFile = baos.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.imgbb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ImgbbApi::class.java)

        api.uploadImage(imgbbApiKey, body).enqueue(object : Callback<ImgbbResponse> {
            override fun onResponse(call: Call<ImgbbResponse>, response: Response<ImgbbResponse>) {
                btnConfirm.isEnabled = true
                if (response.isSuccessful && response.body() != null) {
                    val imageUrl = response.body()!!.data.url
                    Toast.makeText(this@MainActivity, "Đăng tin thành công!", Toast.LENGTH_SHORT).show()

                    // ==========================================
                    // ĐOẠN CODE MỚI: CHUYỂN DỮ LIỆU SANG TRANG CHỦ
                    // ==========================================
                    val intent = Intent(this@MainActivity, DashboardActivity::class.java)

                    // Lấy dữ liệu từ các ô nhập
                    intent.putExtra("TITLE", edtTitle.text.toString().trim())
                    intent.putExtra("PRICE", edtPrice.text.toString().trim() + " VNĐ")
                    intent.putExtra("ADDRESS", edtAddress.text.toString().trim())
                    intent.putExtra("DESC", edtDescription.text.toString().trim())

                    // Đóng gói Link ảnh vào danh sách và gửi đi
                    val imageList = ArrayList<String>()
                    imageList.add(imageUrl)
                    intent.putStringArrayListExtra("IMAGES", imageList)

                    // Chuyển trang và Đóng màn hình đăng tin lại
                    startActivity(intent)
                    finish()
                    // ==========================================
                }
            }

            override fun onFailure(call: Call<ImgbbResponse>, t: Throwable) {
                btnConfirm.isEnabled = true
                Toast.makeText(this@MainActivity, "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}