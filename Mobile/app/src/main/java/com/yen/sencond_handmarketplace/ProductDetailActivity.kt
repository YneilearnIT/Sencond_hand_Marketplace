package com.yen.sencond_handmarketplace

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ProductDetailActivity : AppCompatActivity() {

    private var currentCartCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // --- 1. ÁNH XẠ VIEW ---
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnCartHeader = findViewById<ImageButton>(R.id.btnCartDetail)
        val txtBadge = findViewById<TextView>(R.id.txtCartBadge)
        val edtSearch = findViewById<EditText>(R.id.edtSearchDetail)

        val imgProduct = findViewById<ImageView>(R.id.imgProductLarge)
        val txtPrice = findViewById<TextView>(R.id.txtPriceDetail)
        val txtName = findViewById<TextView>(R.id.txtNameDetail)
        val txtAddress = findViewById<TextView>(R.id.txtAddressDetail)
        val txtDesc = findViewById<TextView>(R.id.txtDescDetail)

        val txtCondition = findViewById<TextView>(R.id.txtStatusDetail)

        val btnReport = findViewById<LinearLayout>(R.id.btnReport)
        val btnChat = findViewById<LinearLayout>(R.id.btnBottomChat)
        val btnAddToCart = findViewById<ImageButton>(R.id.btnBottomAddToCart)
        val btnBuyNow = findViewById<Button>(R.id.btnBottomBuyNow)

        // --- 2. ĐỔ DỮ LIỆU TỪ INTENT ---
        // Lấy dữ liệu và kiểm tra kỹ Key (Phải khớp 100% với Dashboard gửi đi)
        val title = intent.getStringExtra("TITLE") ?: "Sản phẩm"
        val price = intent.getStringExtra("PRICE") ?: "Liên hệ"
        val addr = intent.getStringExtra("ADDR") ?: "N/A"
        val desc = intent.getStringExtra("DESC") ?: ""
        val images = intent.getStringArrayListExtra("IMAGES")

        // KIỂM TRA KEY NÀY: Trong Dashboard bạn gửi là "CONDITION" thì ở đây phải là "CONDITION"
        val condition = intent.getStringExtra("CONDITION")

        txtName.text = title
        txtPrice.text = if (price.contains("đ")) price else "$price đ"
        txtAddress.text = addr
        txtDesc.text = desc

        // HIỂN THỊ ĐỘ MỚI LÊN UI
        if (txtCondition != null) {
            if (condition != null) {
                txtCondition.text = "Tình trạng: $condition"
            } else {
                // Nếu bị null, hiển thị thông báo để bạn biết đường sửa lỗi Key
                txtCondition.text = "Tình trạng: Chưa cập nhật"
            }
        }

        if (!images.isNullOrEmpty()) {
            Glide.with(this).load(images[0]).into(imgProduct)
        }

        // --- 3. XỬ LÝ SỰ KIỆN ---
        btnBack.setOnClickListener { finish() }

        btnCartHeader.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        edtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyword = edtSearch.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    val searchIntent = Intent(this, SearchResultsActivity::class.java)
                    searchIntent.putExtra("SEARCH_KEYWORD", keyword)
                    startActivity(searchIntent)
                }
                true
            } else false
        }

        btnAddToCart.setOnClickListener {
            currentCartCount++
            txtBadge.text = currentCartCount.toString()
            txtBadge.visibility = View.VISIBLE
            Toast.makeText(this, "Đã thêm vào giỏ!", Toast.LENGTH_SHORT).show()
        }

        btnBuyNow.setOnClickListener {
            val checkoutIntent = Intent(this, CheckoutActivity::class.java)
            checkoutIntent.putExtra("TITLE", title)
            checkoutIntent.putExtra("PRICE", price)
            startActivity(checkoutIntent)
        }

        btnChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        btnReport.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }
    }
}