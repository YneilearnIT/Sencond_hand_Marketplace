package com.yen.sencond_handmarketplace

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PromotionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_promotion_detail)

        // 1. Xử lý tràn viền (Edge-to-Edge) - Giúp Header không bị lấp bởi thanh hệ thống
        val headerPush = findViewById<android.view.View>(R.id.headerPush)
        if (headerPush != null) {
            ViewCompat.setOnApplyWindowInsetsListener(headerPush) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // 2. Ánh xạ các View từ XML
        val btnBack = findViewById<ImageButton>(R.id.btnBackPush)
        val imgAds = findViewById<ImageView>(R.id.imgPackageAds)
        val txtName = findViewById<TextView>(R.id.txtPackageName)
        val txtPrice = findViewById<TextView>(R.id.txtPackagePrice)
        val txtDesc = findViewById<TextView>(R.id.txtPackageDesc)
        val txtDuration = findViewById<TextView>(R.id.txtDuration)
        val btnBuy = findViewById<Button>(R.id.btnBuyPackage)

        // 3. Nhận dữ liệu CHÍNH XÁC từ PromotionActivity gửi sang
        val packageName = intent.getStringExtra("PACKAGE_NAME") ?: "Gói dịch vụ"
        val packagePrice = intent.getStringExtra("PACKAGE_PRICE") ?: "Liên hệ"
        val packageDesc = intent.getStringExtra("PACKAGE_DESC") ?: ""
        val packageDuration = intent.getStringExtra("PACKAGE_DURATION") ?: ""
        val packageImage = intent.getIntExtra("PACKAGE_IMAGE", android.R.drawable.ic_menu_gallery)

        // 4. HIỂN THỊ DỮ LIỆU THẬT (Xóa bỏ dữ liệu mẫu trong XML)
        txtName.text = packageName
        txtPrice.text = packagePrice
        txtDesc.text = packageDesc
        txtDuration.text = packageDuration
        imgAds.setImageResource(packageImage)

        // 5. Xử lý sự kiện các nút bấm

        // Nút quay lại
        btnBack.setOnClickListener {
            finish()
        }

        // Nút Thanh toán ngay -> Chuyển sang QR với dữ liệu đã chọn
        btnBuy.setOnClickListener {
            // Khởi tạo Intent chuyển sang màn hình QRPaymentActivity
            val intentPay = Intent(this, QRPaymentActivity::class.java)

            // Truyền tiếp dữ liệu thật sang màn hình thanh toán
            intentPay.putExtra("PAYMENT_AMOUNT", packagePrice)
            intentPay.putExtra("PAYMENT_ITEM", packageName)

            startActivity(intentPay)

            Toast.makeText(this, "Mở mã QR cho: $packageName", Toast.LENGTH_SHORT).show()
        }
    }
}