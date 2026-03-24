package com.yen.sencond_handmarketplace

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Kết nối với file XML giỏ hàng của bạn
        setContentView(R.layout.activity_cart)

        // 1. Ánh xạ các nút từ XML
        val btnBack = findViewById<ImageButton>(R.id.btnBackCart)
        val btnEdit = findViewById<android.widget.TextView>(R.id.btnEditCart)
        val cbSelectAll = findViewById<CheckBox>(R.id.cbSelectAll)
        val btnCheckout = findViewById<Button>(R.id.btnCheckout)

        // 2. Xử lý nút Quay lại
        btnBack.setOnClickListener {
            finish() // Đóng màn hình giỏ hàng, quay về Dashboard
        }

        // 3. Xử lý nút Chỉnh sửa
        btnEdit.setOnClickListener {
            Toast.makeText(this, "Tính năng chỉnh sửa đang phát triển", Toast.LENGTH_SHORT).show()
        }

        // 4. Xử lý Checkbox Chọn tất cả
        cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Đã chọn tất cả sản phẩm", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. Xử lý nút Thanh toán -> Qua màn hình Checkout
        btnCheckout.setOnClickListener {
            // Sau này bạn sẽ tạo CheckoutActivity để chuyển sang đây
            // val intent = Intent(this, CheckoutActivity::class.java)
            // startActivity(intent)
            Toast.makeText(this, "Đang chuyển sang thanh toán...", Toast.LENGTH_SHORT).show()
        }
    }
}