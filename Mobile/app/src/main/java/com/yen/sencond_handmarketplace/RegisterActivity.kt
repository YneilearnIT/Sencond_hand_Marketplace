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

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // 1. Xử lý tràn viền (Sửa lỗi đỏ R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. Khai báo các View từ XML
        val edtAccount = findViewById<EditText>(R.id.edtRegisterAccount)
        val edtPassword = findViewById<EditText>(R.id.edtRegisterPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val txtGoToLogin = findViewById<TextView>(R.id.txtGoToLogin)

        // 3. Sự kiện khi nhấn "Đã có tài khoản?" -> Chuyển về màn hình Login
        txtGoToLogin.setOnClickListener {
            // Giả sử màn hình đăng nhập của bạn tên là LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Đóng màn hình đăng ký để không bị quay lại khi bấm Back
        }

        // 4. Sự kiện khi nhấn nút "Đăng ký"
        btnRegister.setOnClickListener {
            val account = edtAccount.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            // Kiểm tra dữ liệu đơn giản
            if (account.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this, "Mật khẩu phải từ 6 ký tự trở lên!", Toast.LENGTH_SHORT).show()
            } else {
                // Tại đây bạn sẽ viết code gửi dữ liệu lên SQL Server hoặc Firebase
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_LONG).show()

                // Sau khi đăng ký xong thường sẽ tự chuyển sang màn hình chính hoặc login
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}