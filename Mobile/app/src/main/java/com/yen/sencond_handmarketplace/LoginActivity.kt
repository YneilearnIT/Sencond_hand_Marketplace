package com.yen.sencond_handmarketplace

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Kích hoạt chế độ tràn viền
        enableEdgeToEdge()
        setContentView(R.layout.activity_login) // Đảm bảo đúng tên file XML của bạn

        // 2. Sửa lỗi ID đỏ "main" - Thiết lập Padding để không bị lẹm vào thanh hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 3. Khai báo các thành phần giao diện (Views)
        val edtAccount = findViewById<EditText>(R.id.edtAccount)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnForgot = findViewById<TextView>(R.id.btnForgotPassword)

        // 4. Xử lý sự kiện nút Đăng nhập
        btnLogin.setOnClickListener {
            val account = edtAccount.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            // Kiểm tra xem người dùng đã nhập đủ chưa
            if (account.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tài khoản và mật khẩu!", Toast.LENGTH_SHORT).show()
            } else {
                // Giả lập kiểm tra đăng nhập thành công
                if (account == "admin" && password == "123456") {
                    // Giả sử đăng nhập thành công và bạn có biến userName
                    val userName = "Nguyễn Văn A"

// Lưu vào SharedPreferences
                    val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putString("USER_NAME", userName)
                    editor.putBoolean("IS_LOGGED_IN", true)
                    editor.apply()


                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
// Chuyển sang màn hình Dashboard (hoặc màn hình chính của bạn)
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish() // Đóng màn hình đăng nhập

                } else {
                    Toast.makeText(this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 5. Xử lý sự kiện Quên mật khẩu
        btnForgot.setOnClickListener {
            // Chuyển sang màn hình quên mật khẩu (nếu bạn đã tạo)
            Toast.makeText(this, "Chức năng đang được cập nhật!", Toast.LENGTH_SHORT).show()
        }
    }
}