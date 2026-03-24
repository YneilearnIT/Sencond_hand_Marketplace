package com.yen.sencond_handmarketplace

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide // Phụ tá tải ảnh mạng

// 1. Cấu trúc lưu trữ 1 sản phẩm
data class Product(
    val title: String,
    val price: String,
    val address: String,
    val description: String,
    val imageUrls: List<String>
)

class DashboardActivity : AppCompatActivity() {

    // 2. Database ảo (Sẽ lưu các sản phẩm bạn vừa đăng để không bị mất khi chuyển trang)
    companion object {
        val databaseTamThoi = mutableListOf<Product>()
    }

    private lateinit var layoutProductList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        layoutProductList = findViewById(R.id.layoutProductList)

        // Bắt sự kiện bấm nút Dấu + để đi đăng tin
        val btnNavAdd = findViewById<ImageButton>(R.id.btnNavAdd)
        btnNavAdd.setOnClickListener {
            val intent = Intent(this, PostingActivity::class.java)
            startActivity(intent)
        }

        // 3. HỨNG DỮ LIỆU TỪ TRANG ĐĂNG TIN NÉM SANG
        val newTitle = intent.getStringExtra("TITLE")
        if (newTitle != null) {
            val newPrice = intent.getStringExtra("PRICE") ?: ""
            val newAddress = intent.getStringExtra("ADDRESS") ?: ""
            val newDesc = intent.getStringExtra("DESC") ?: ""
            val newImages = intent.getStringArrayListExtra("IMAGES") ?: arrayListOf()

            // Gom lại thành 1 sản phẩm và nhét lên ĐẦU danh sách ảo
            databaseTamThoi.add(0, Product(newTitle, newPrice, newAddress, newDesc, newImages))

            // Xóa dữ liệu cũ trong túi hàng để xoay màn hình không bị đăng lại 2 lần
            intent.removeExtra("TITLE")
        }

        // 4. HIỂN THỊ TẤT CẢ SẢN PHẨM RA MÀN HÌNH
        for (product in databaseTamThoi) {
            addProductToUI(product)
        }
    }

    // Hàm phụ: Lấy khuôn (item_product) -> Đổ chữ & ảnh -> Nhét lên màn hình
    private fun addProductToUI(product: Product) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_product, layoutProductList, false)

        val tvTitle = view.findViewById<TextView>(R.id.itemTvTitle)
        val tvPrice = view.findViewById<TextView>(R.id.itemTvPrice)
        val tvAddress = view.findViewById<TextView>(R.id.itemTvAddress)
        val tvDesc = view.findViewById<TextView>(R.id.itemTvDescription)
        val viewFlipper = view.findViewById<ViewFlipper>(R.id.itemViewFlipper)

        tvTitle.text = product.title
        tvPrice.text = product.price
        tvAddress.text = product.address
        tvDesc.text = product.description

        // Đổ ảnh vào (Nếu có nhiều ảnh sẽ tự động chạy slide)
        if (product.imageUrls.isNotEmpty()) {
            for (url in product.imageUrls) {
                val imageView = ImageView(this)
                imageView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                // Glide ơi, tải link ảnh này nhét vào ImageView cho tao!
                Glide.with(this).load(url).into(imageView)

                viewFlipper.addView(imageView)
            }
            if (product.imageUrls.size <= 1) {
                viewFlipper.stopFlipping() // Có 1 ảnh thì khỏi chạy slide
            }
        } else {
            viewFlipper.visibility = android.view.View.GONE // Không có ảnh thì giấu luôn ô ảnh
        }

        // Cuối cùng: Dán cái khuôn đã điền đầy đủ thông tin lên màn hình Trang chủ!
        layoutProductList.addView(view)
    }
}