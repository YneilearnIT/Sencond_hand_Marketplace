package com.yen.sencond_handmarketplace

import android.content.Intent // Import thêm Intent để chuyển trang
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton // Import ImageButton cho Bottom Nav
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class DashboardActivity : AppCompatActivity() {

    private lateinit var layoutProductList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        layoutProductList = findViewById(R.id.layoutProductList)

        // ==========================================
        // 1. CHỨC NĂNG CHUYỂN TRANG (NÚT DẤU CỘNG)
        // ==========================================
        val btnNavAdd = findViewById<ImageButton>(R.id.btnNavAdd)
        btnNavAdd.setOnClickListener {
            // Chuyển sang trang Đăng tin (MainActivity)
            val intent = Intent(this@DashboardActivity, PostingActivity::class.java)
            startActivity(intent)
        }
        // ==========================================

        // 2. MỞ CỬA HỨNG DỮ LIỆU TỪ MÀN HÌNH ĐĂNG TIN GỬI SANG
        val newTitle = intent.getStringExtra("TITLE")

        if (newTitle != null) {
            // Nếu có người vừa đăng tin, lấy toàn bộ dữ liệu ra
            val newPrice = intent.getStringExtra("PRICE") ?: ""
            val newAddress = intent.getStringExtra("ADDRESS") ?: ""
            val newDesc = intent.getStringExtra("DESC") ?: ""
            val newImages = intent.getStringArrayListExtra("IMAGES") ?: arrayListOf()

            // GỌI HÀM NHÉT SẢN PHẨM MỚI LÊN ĐẦU TIÊN
            addNewProductReal(newTitle, newPrice, newAddress, newDesc, newImages)
        } else {
            // Nếu chỉ mở app lên xem (không đăng tin), tôi tạo 1 cái tin mẫu cho bạn dễ nhìn
            val mockImages = arrayListOf("https://i.ibb.co/30Z0Zcc/macbook.jpg") // Ảnh ví dụ
            addNewProductReal("Macbook Pro M1 Mẫu", "18,000,000 VNĐ", "Quận 1, TP.HCM", "Đây là tin đăng mẫu", mockImages)
        }
    }

    // HÀM XỬ LÝ: Đổ dữ liệu thật và danh sách ảnh vào cái "Khuôn" (item_product.xml)
    private fun addNewProductReal(title: String, price: String, address: String, description: String, imageUrls: List<String>) {
        val productView = LayoutInflater.from(this).inflate(R.layout.item_product, layoutProductList, false)

        val tvTitle = productView.findViewById<TextView>(R.id.itemTvTitle)
        val tvPrice = productView.findViewById<TextView>(R.id.itemTvPrice)
        val tvAddress = productView.findViewById<TextView>(R.id.itemTvAddress)
        val tvDesc = productView.findViewById<TextView>(R.id.itemTvDescription)
        val viewFlipper = productView.findViewById<ViewFlipper>(R.id.itemViewFlipper)

        // Đổ chữ vào
        tvTitle.text = title
        tvPrice.text = price
        tvAddress.text = address
        tvDesc.text = description

        // Đổ danh sách ảnh vào ViewFlipper để làm Slide tự chạy
        if (imageUrls.isNotEmpty()) {
            for (url in imageUrls) {
                val imageView = ImageView(this)
                imageView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                // Glide sẽ phụ trách việc tải ảnh từ đường link URL hiển thị lên màn hình
                Glide.with(this).load(url).into(imageView)

                viewFlipper.addView(imageView)
            }

            // Tắt hiệu ứng tự chạy nếu người dùng chỉ đăng 1 ảnh
            if (imageUrls.size <= 1) {
                viewFlipper.stopFlipping()
            }
        } else {
            // Nếu không có ảnh nào, để tạm cái icon mặc định
            val imageView = ImageView(this)
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            viewFlipper.addView(imageView)
            viewFlipper.stopFlipping()
        }

        // LỆNH QUAN TRỌNG NHẤT: Thêm sản phẩm này vào vị trí số 0 (Nhảy lên trên cùng)
        layoutProductList.addView(productView, 0)
    }
}