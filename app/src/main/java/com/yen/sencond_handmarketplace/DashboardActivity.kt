package com.yen.sencond_handmarketplace

<<<<<<< HEAD
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

=======
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

>>>>>>> 7ec05b6a5ccc8b019cca11c35ccd913321cfd47f
    private lateinit var layoutProductList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        layoutProductList = findViewById(R.id.layoutProductList)

<<<<<<< HEAD
        // Bắt sự kiện bấm nút Dấu + để đi đăng tin
        val btnNavAdd = findViewById<ImageButton>(R.id.btnNavAdd)
        btnNavAdd.setOnClickListener {
            val intent = Intent(this, PostingActivity::class.java)
            startActivity(intent)
        }

        // 3. HỨNG DỮ LIỆU TỪ TRANG ĐĂNG TIN NÉM SANG
        val newTitle = intent.getStringExtra("TITLE")
        if (newTitle != null) {
=======
        // ==========================================
        // 1. CHỨC NĂNG CHUYỂN TRANG (NÚT DẤU CỘNG)
        // ==========================================
        val btnNavAdd = findViewById<ImageButton>(R.id.btnNavAdd)
        btnNavAdd.setOnClickListener {
            // Chuyển sang trang Đăng tin (MainActivity)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        // ==========================================

        // 2. MỞ CỬA HỨNG DỮ LIỆU TỪ MÀN HÌNH ĐĂNG TIN GỬI SANG
        val newTitle = intent.getStringExtra("TITLE")

        if (newTitle != null) {
            // Nếu có người vừa đăng tin, lấy toàn bộ dữ liệu ra
>>>>>>> 7ec05b6a5ccc8b019cca11c35ccd913321cfd47f
            val newPrice = intent.getStringExtra("PRICE") ?: ""
            val newAddress = intent.getStringExtra("ADDRESS") ?: ""
            val newDesc = intent.getStringExtra("DESC") ?: ""
            val newImages = intent.getStringArrayListExtra("IMAGES") ?: arrayListOf()

<<<<<<< HEAD
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
=======
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
>>>>>>> 7ec05b6a5ccc8b019cca11c35ccd913321cfd47f
                val imageView = ImageView(this)
                imageView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP

<<<<<<< HEAD
                // Glide ơi, tải link ảnh này nhét vào ImageView cho tao!
=======
                // Glide sẽ phụ trách việc tải ảnh từ đường link URL hiển thị lên màn hình
>>>>>>> 7ec05b6a5ccc8b019cca11c35ccd913321cfd47f
                Glide.with(this).load(url).into(imageView)

                viewFlipper.addView(imageView)
            }
<<<<<<< HEAD
            if (product.imageUrls.size <= 1) {
                viewFlipper.stopFlipping() // Có 1 ảnh thì khỏi chạy slide
            }
        } else {
            viewFlipper.visibility = android.view.View.GONE // Không có ảnh thì giấu luôn ô ảnh
        }

        // Cuối cùng: Dán cái khuôn đã điền đầy đủ thông tin lên màn hình Trang chủ!
        layoutProductList.addView(view)
=======

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
>>>>>>> 7ec05b6a5ccc8b019cca11c35ccd913321cfd47f
    }
}