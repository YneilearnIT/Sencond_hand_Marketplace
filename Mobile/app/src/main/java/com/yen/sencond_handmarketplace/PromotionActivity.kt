package com.yen.sencond_handmarketplace

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PromotionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_promotion)


        // 2. Ánh xạ View
        val btnBack = findViewById<ImageButton>(R.id.btnBackPush)
        val btnNavHome = findViewById<ImageButton>(R.id.btnNavHome)
        val btnNavProfile = findViewById<ImageButton>(R.id.btnNavProfile)
        val btnNavAdd = findViewById<ImageButton>(R.id.btnNavAdd)

        // 3. Xử lý nút quay lại ở Header (Góc trái)
        btnBack.setOnClickListener {
            finish() // Quay lại màn hình vừa đứng trước đó
        }

        // 4. Xử lý thanh điều hướng Bottom Nav

        // Bấm hình ngôi nhà -> Về Dashboard
        btnNavHome.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            // Xóa các màn hình cũ để về Home sạch sẽ
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }


        // Bấm hình Profile -> Qua màn hình User (Nếu đã đăng nhập)
        btnNavProfile.setOnClickListener {
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)

            if (isLoggedIn) {
                startActivity(Intent(this, InfoUserActivity::class.java))
            } else {
                Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }


    }
}