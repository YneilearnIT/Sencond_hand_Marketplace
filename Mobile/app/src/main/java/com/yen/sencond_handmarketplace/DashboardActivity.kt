package com.yen.sencond_handmarketplace

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var layoutProductList: LinearLayout
    private lateinit var txtUserStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // 1. ÁNH XẠ VIEW
        txtUserStatus = findViewById(R.id.txtGoToRegister)
        layoutProductList = findViewById(R.id.layoutProductList)
        val btnNavHome = findViewById<ImageButton>(R.id.btnNavHome)
        val btnNavPromo = findViewById<ImageButton>(R.id.btnNavUpload)
        val btnProfile = findViewById<ImageButton>(R.id.btnNavProfile)
        val btnNavAdd = findViewById<ImageButton>(R.id.btnNavAdd)

        // 2. XỬ LÝ NHẤN CHỮ "ĐĂNG KÝ" (Ở HEADER)
        txtUserStatus.setOnClickListener {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)

            if (!isLoggedIn) {
                startActivity(Intent(this, RegisterActivity::class.java))
            } else {
                // Nếu đã đăng nhập, bấm vào tên có thể mở Profile luôn cho tiện
                startActivity(Intent(this, InfoUserActivity::class.java))
            }
        }
        // 1. NHẤN HÌNH NGÔI NHÀ (HOME)
        btnNavHome.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

        // 2. NHẤN HÌNH SỐ 2 (PROMOTION)
        btnNavPromo.setOnClickListener {
            // Chuyển sang màn hình PromotionActivity
            val intent = Intent(this, PromotionActivity::class.java)
            startActivity(intent)
        }

        // 3. XỬ LÝ NHẤN ICON AVATAR (Ở BOTTOM NAV)
        btnProfile.setOnClickListener {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)

            if (isLoggedIn) {
                // Nếu ĐÃ đăng nhập: Sang màn hình Info
                startActivity(Intent(this, InfoUserActivity::class.java))
            } else {
                // Nếu CHƯA đăng nhập: Báo lỗi và bắt sang Login
                Toast.makeText(this, "Vui lòng đăng nhập để xem thông tin!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }


    }

    // 5. CẬP NHẬT GIAO DIỆN MỖI KHI QUAY LẠI (TỪ LOGIN/INFO TRỞ VỀ)
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