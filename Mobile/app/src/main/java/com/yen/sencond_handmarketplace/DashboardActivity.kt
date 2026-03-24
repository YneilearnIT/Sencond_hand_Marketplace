package com.yen.sencond_handmarketplace

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

// 1. Cấu trúc lưu trữ 1 sản phẩm (Đã thêm biến condition)
data class Product(
    val title: String,
    val price: String,
    val address: String,
    val description: String,
    val imageUrls: List<String>,
    val category: String = "",
    val condition: String // Thêm thuộc tính độ mới
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
        val btnCart = findViewById<ImageView>(R.id.btnCart)
        val tvDanhMuc = findViewById<TextView>(R.id.btnOpenCategories)

        val btnNavHome = findViewById<ImageButton>(R.id.btnNavHome)
        val btnNavPromo = findViewById<ImageButton>(R.id.btnNavUpload)
        val btnProfile = findViewById<ImageButton>(R.id.btnNavProfile)
        val btnNavAdd = findViewById<ImageButton>(R.id.btnNavAdd)
        val edtSearch = findViewById<EditText>(R.id.edtSearchDashboard)

        // --- XỬ LÝ TÌM KIẾM ---
        edtSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyword = edtSearch.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    val intent = Intent(this, SearchResultsActivity::class.java)
                    intent.putExtra("SEARCH_KEYWORD", keyword)
                    startActivity(intent)
                    edtSearch.text.clear()
                } else {
                    Toast.makeText(this, "Vui lòng nhập từ khóa để tìm!", Toast.LENGTH_SHORT).show()
                }
                true
            } else false
        }

        // --- XỬ LÝ CLICK NAVIGATION ---
        txtUserStatus.setOnClickListener {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            if (!sharedPref.getBoolean("IS_LOGGED_IN", false)) {
                startActivity(Intent(this, RegisterActivity::class.java))
            } else {
                startActivity(Intent(this, InfoUserActivity::class.java))
            }
        }

        btnNavHome.setOnClickListener { refreshUI(databaseTamThoi) }

        btnProfile.setOnClickListener {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            if (sharedPref.getBoolean("IS_LOGGED_IN", false)) {
                startActivity(Intent(this, InfoUserActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
        // Ánh xạ lại đúng ID trong XML của bạn
        btnNavPromo.setOnClickListener {
            startActivity(Intent(this, PromotionActivity::class.java))
        }

        btnCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        btnNavAdd.setOnClickListener {
            startActivity(Intent(this, PostingActivity::class.java))
        }

        tvDanhMuc.setOnClickListener { view -> showCategoriesPopup(view) }

        // --- HỨNG DỮ LIỆU ĐĂNG TIN ---
        val newTitle = intent.getStringExtra("TITLE")
        if (newTitle != null) {
            val newPrice = intent.getStringExtra("PRICE") ?: ""
            val newAddress = intent.getStringExtra("ADDRESS") ?: ""
            val newDesc = intent.getStringExtra("DESC") ?: ""
            val newImages = intent.getStringArrayListExtra("IMAGES") ?: arrayListOf()
            // Lấy độ mới từ key "CATEGORY" (hoặc bạn đổi thành "CONDITION" cho đồng nhất)
            val newCond = intent.getStringExtra("CATEGORY") ?: "Mới 95%"

            // Lưu vào database ảo với đầy đủ tham số
            databaseTamThoi.add(0, Product(newTitle, newPrice, newAddress, newDesc, newImages, "Khác", newCond))
            intent.removeExtra("TITLE")
        }

        refreshUI(databaseTamThoi)
    }

    private fun refreshUI(list: List<Product>) {
        layoutProductList.removeAllViews()
        for (product in list) {
            addProductToUI(product)
        }
    }

    private fun addProductToUI(product: Product) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_product, layoutProductList, false)

        view.findViewById<TextView>(R.id.itemTvTitle).text = product.title
        view.findViewById<TextView>(R.id.itemTvPrice).text = "${product.price} đ"
        view.findViewById<TextView>(R.id.itemTvAddress).text = product.address
        view.findViewById<TextView>(R.id.itemTvDescription).text = product.description

        // Hiển thị độ mới ngay trên item nếu layout của bạn có hỗ trợ
        val txtCond = view.findViewById<TextView>(R.id.txtProductCondition)
        if (txtCond != null) {
            txtCond.text = product.condition
        }

        view.setOnClickListener {
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("TITLE", product.title)
            intent.putExtra("PRICE", product.price)
            intent.putExtra("ADDR", product.address)
            intent.putExtra("DESC", product.description)

            // --- GỬI ĐỘ MỚI SANG MÀN HÌNH CHI TIẾT ---
            intent.putExtra("CONDITION", product.condition)

            intent.putStringArrayListExtra("IMAGES", ArrayList(product.imageUrls))
            startActivity(intent)
        }

        val viewFlipper = view.findViewById<ViewFlipper>(R.id.itemViewFlipper)
        if (product.imageUrls.isNotEmpty()) {
            for (url in product.imageUrls) {
                val imageView = ImageView(this)
                imageView.layoutParams = ViewGroup.LayoutParams(-1, -1)
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(this).load(url).into(imageView)
                viewFlipper.addView(imageView)
            }
            if (product.imageUrls.size <= 1) viewFlipper.stopFlipping()
        } else {
            viewFlipper.visibility = View.GONE
        }
        layoutProductList.addView(view)
    }

    private fun showCategoriesPopup(anchorView: View) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_categories, null)
        val popupWindow = PopupWindow(popupView, 550, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        val cats = mapOf(
            R.id.tvCatQuanAo to "Quần áo",
            R.id.tvCatGiayDep to "Giày dép",
            R.id.tvCatDienTu to "Điện tử",
            R.id.tvCatGiaDung to "Đồ gia dụng"
        )

        for ((id, name) in cats) {
            popupView.findViewById<TextView>(id).setOnClickListener {
                val filteredList = databaseTamThoi.filter { it.category == name }
                refreshUI(filteredList)
                popupWindow.dismiss()
            }
        }
        popupWindow.showAsDropDown(anchorView, 0, 10)
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)
        val userName = sharedPref.getString("USER_NAME", "Đăng ký")

        txtUserStatus.text = if (isLoggedIn) userName else "Đăng ký"
        refreshUI(databaseTamThoi)
    }
}