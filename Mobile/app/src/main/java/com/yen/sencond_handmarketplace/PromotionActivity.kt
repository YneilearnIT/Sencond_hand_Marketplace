package com.yen.sencond_handmarketplace

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PromotionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_promotion)

        // --- 1. ÁNH XẠ VIEW NAVIGATION ---
        val btnBack = findViewById<ImageButton>(R.id.btnBackPush)
        val btnNavHome = findViewById<ImageButton>(R.id.btnNavHome)
        val btnNavProfile = findViewById<ImageButton>(R.id.btnNavProfile)

        // --- 2. ÁNH XẠ CÁC GÓI QUẢNG CÁO (Kiểm tra ID trong XML của bạn) ---
        // Giả sử ID CardView Gói Cơ Bản là layoutBasic, Gói VIP là layoutVip
        val layoutBasic = findViewById<CardView>(R.id.cardPromoBasic)
        val layoutVip = findViewById<CardView>(R.id.cardPromoVIP)


        // --- 3. THÊM MỚI: XỬ LÝ CLICK CHUYỂN QUA CHI TIẾT GÓI ---

        // Click Gói Cơ Bản
        layoutBasic?.setOnClickListener {
            val intent = Intent(this, PromotionDetailActivity::class.java)
            intent.putExtra("PACKAGE_NAME", "Gói Đẩy Tin Cơ Bản")
            intent.putExtra("PACKAGE_PRICE", "15.000 đ")
            intent.putExtra("PACKAGE_DURATION", "Thời gian hiệu lực: 24 Giờ kể từ khi kích hoạt")
            intent.putExtra("PACKAGE_DESC", "• Đưa tin lên đầu trong 1 ngày.\n• Phù hợp cho các mặt hàng bán nhanh.\n• Tiết kiệm chi phí tối đa.")
            intent.putExtra("PACKAGE_IMAGE", android.R.drawable.ic_menu_send)
            startActivity(intent)
        }

        // Click Gói VIP
        layoutVip?.setOnClickListener {
            val intent = Intent(this, PromotionDetailActivity::class.java)
            intent.putExtra("PACKAGE_NAME", "Gói Đẩy Tin VIP 7 Ngày")
            intent.putExtra("PACKAGE_PRICE", "49.000 đ")
            intent.putExtra("PACKAGE_DURATION", "Thời gian hiệu lực: 07 ngày kể từ khi kích hoạt")
            intent.putExtra("PACKAGE_DESC", "• Đưa sản phẩm lên đầu danh mục tìm kiếm.\n" +
                    "• Hiển thị nhãn VIP nổi bật trên ảnh sản phẩm.\n" +
                    "• Tiếp cận gấp 5 lần khách hàng tiềm năng.\n" +
                    "• Ưu tiên hiển thị trong mục Gợi ý hôm nay.")
            intent.putExtra("PACKAGE_IMAGE", android.R.drawable.ic_menu_agenda)
            startActivity(intent)
        }


        // --- 4. GIỮ NGUYÊN CÁC SỰ KIỆN CŨ ---

        // Nút quay lại ở Header
        btnBack.setOnClickListener {
            finish()
        }

        // Bấm hình ngôi nhà -> Về Dashboard
        btnNavHome.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Bấm hình Profile -> Qua màn hình User
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