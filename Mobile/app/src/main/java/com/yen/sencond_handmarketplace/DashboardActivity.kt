package com.yen.sencond_handmarketplace

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

// 1. Cấu trúc lưu trữ 1 sản phẩm
data class Product(
    val title: String,
    val price: String,
    val address: String,
    val description: String,
    val imageUrls: List<String>
)

class DashboardActivity : AppCompatActivity() {

    // 2. Database ảo
    companion object {
        val databaseTamThoi = mutableListOf<Product>()
    }

    private lateinit var layoutProductList: LinearLayout
    private lateinit var txtUserStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // --- ÁNH XẠ VIEW ---
        txtUserStatus = findViewById(R.id.txtGoToRegister)
        layoutProductList = findViewById(R.id.layoutProductList)
        val btnNavHome = findViewById<ImageButton>(R.id.btnNavHome)
        val btnNavPromo = findViewById<ImageButton>(R.id.btnNavUpload)
        val btnProfile = findViewById<ImageButton>(R.id.btnNavProfile)
        val btnNavAdd = findViewById<ImageButton>(R.id.btnNavAdd)

        // --- XỬ LÝ CLICK ---
        txtUserStatus.setOnClickListener {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)
            if (!isLoggedIn) {
                startActivity(Intent(this, RegisterActivity::class.java))
            } else {
                startActivity(Intent(this, InfoUserActivity::class.java))
            }
        }

        btnNavHome.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        btnNavPromo.setOnClickListener {
            startActivity(Intent(this, PromotionActivity::class.java))
        }

        btnProfile.setOnClickListener {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)
            if (isLoggedIn) {
                startActivity(Intent(this, InfoUserActivity::class.java))
            } else {
                Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        btnNavAdd.setOnClickListener {
            startActivity(Intent(this, PostingActivity::class.java))
        }

        // --- HỨNG DỮ LIỆU TỪ TRANG ĐĂNG TIN ---
        val newTitle = intent.getStringExtra("TITLE")
        if (newTitle != null) {
            val newPrice = intent.getStringExtra("PRICE") ?: ""
            val newAddress = intent.getStringExtra("ADDRESS") ?: ""
            val newDesc = intent.getStringExtra("DESC") ?: ""
            val newImages = intent.getStringArrayListExtra("IMAGES") ?: arrayListOf()

            databaseTamThoi.add(0, Product(newTitle, newPrice, newAddress, newDesc, newImages))
            intent.removeExtra("TITLE")
        }

        // Hiển thị sản phẩm (Chỉ chạy 1 lần trong onCreate)
        layoutProductList.removeAllViews()
        for (product in databaseTamThoi) {
            addProductToUI(product)
        }
    }

    // Hàm phụ: Đổ dữ liệu lên UI
    private fun addProductToUI(product: Product) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_product, layoutProductList, false)

        view.findViewById<TextView>(R.id.itemTvTitle).text = product.title
        view.findViewById<TextView>(R.id.itemTvPrice).text = product.price
        view.findViewById<TextView>(R.id.itemTvAddress).text = product.address
        view.findViewById<TextView>(R.id.itemTvDescription).text = product.description

        val viewFlipper = view.findViewById<ViewFlipper>(R.id.itemViewFlipper)
        if (product.imageUrls.isNotEmpty()) {
            for (url in product.imageUrls) {
                val imageView = ImageView(this)
                imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(this).load(url).into(imageView)
                viewFlipper.addView(imageView)
            }
            if (product.imageUrls.size <= 1) viewFlipper.stopFlipping()
        } else {
            viewFlipper.visibility = android.view.View.GONE
        }
        layoutProductList.addView(view)
    }

    // --- CẬP NHẬT KHI QUAY LẠI (TỪ LOGIN/INFO TRỞ VỀ) ---
    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)
        val userName = sharedPref.getString("USER_NAME", "Đăng ký")

        if (isLoggedIn) {
            txtUserStatus.text = userName
        } else {
            txtUserStatus.text = "Đăng ký"
        }
    }
}