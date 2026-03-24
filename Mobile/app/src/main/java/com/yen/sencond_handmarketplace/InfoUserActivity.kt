package com.yen.sencond_handmarketplace // Kiểm tra lại package của bạn

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class InfoUserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_info_user) // Đảm bảo đúng tên file XML bạn vừa gửi

        // 1. Sửa lỗi "đỏ ID" cho phần tràn viền
        // Lưu ý: ID ở đây phải là "headerProfile" hoặc thẻ layout ngoài cùng của XML này
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.headerProfile)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. Ánh xạ các View từ XML
        val txtName = findViewById<TextView>(R.id.txtUserName)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnBack = findViewById<ImageButton>(R.id.btnBackProfile)

        // 3. LẤY DỮ LIỆU ĐÃ LƯU TỪ LOGIN
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val nameStored = sharedPref.getString("USER_NAME", "Người dùng ẩn danh")

        // Hiển thị tên lên màn hình
        txtName.text = nameStored

        // 4. XỬ LÝ NÚT ĐĂNG XUẤT
        btnLogout.setOnClickListener {
            val editor = sharedPref.edit()
            editor.clear() // Xóa sạch thông tin đăng nhập
            editor.apply()

            Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show()

            // Quay về Dashboard (Lúc này Dashboard sẽ tự đổi lại chữ "Đăng ký")
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 5. Nút quay lại
        btnBack.setOnClickListener {
            finish()
        }
    }
}