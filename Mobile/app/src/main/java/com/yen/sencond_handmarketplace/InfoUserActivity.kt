package com.yen.sencond_handmarketplace

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class InfoUserActivity : AppCompatActivity() {

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()

            val newName = data?.getStringExtra("NEW_NAME")
            val newContact = data?.getStringExtra("NEW_CONTACT")
            val newAddress = data?.getStringExtra("NEW_ADDRESS")
            val newAvatarUri = data?.getStringExtra("NEW_AVATAR")

            if (!newName.isNullOrEmpty()) {
                findViewById<TextView>(R.id.txtUserName).text = newName
                editor.putString("USER_NAME", newName)
            }
            if (!newContact.isNullOrEmpty()) {
                findViewById<TextView>(R.id.txtUserContact).text = newContact
                editor.putString("USER_CONTACT", newContact)
            }
            if (!newAddress.isNullOrEmpty()) {
                findViewById<TextView>(R.id.txtUserAddress).text = newAddress
                editor.putString("USER_ADDRESS", newAddress)
            }
            if (!newAvatarUri.isNullOrEmpty()) {
                findViewById<ImageView>(R.id.imgUserAvatar).setImageURI(Uri.parse(newAvatarUri))
                editor.putString("USER_AVATAR", newAvatarUri)
            }

            editor.apply()
            Toast.makeText(this, "Đã cập nhật thông tin!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_user)

        // 1. Ánh xạ View
        val txtName = findViewById<TextView>(R.id.txtUserName)
        val txtContact = findViewById<TextView>(R.id.txtUserContact)
        val txtAddress = findViewById<TextView>(R.id.txtUserAddress)
        val txtRole = findViewById<TextView>(R.id.txtUserRole)
        val imgAvatar = findViewById<ImageView>(R.id.imgUserAvatar)

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnBack = findViewById<ImageButton>(R.id.btnBackProfile)
        val btnEdit = findViewById<ImageButton>(R.id.btnEditProfile)
        val btnOrderHistory = findViewById<LinearLayout>(R.id.btnOrderHistory)

        // Nút mới thêm cho Admin
        val btnAdminApprove = findViewById<LinearLayout>(R.id.btnAdminApprove)

        // 2. Hiển thị dữ liệu từ bộ nhớ
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentName = sharedPref.getString("USER_NAME", "Admin")
        val currentContact = sharedPref.getString("USER_CONTACT", "0901.234.567")
        val currentAddress = sharedPref.getString("USER_ADDRESS", "99 Đường ABC, Quận 1")
        val currentRole = sharedPref.getString("USER_ROLE", "Admin") // Mặc định là Admin như bạn muốn

        txtName.text = currentName
        txtContact.text = currentContact
        txtAddress.text = currentAddress
        txtRole.text = currentRole

        val avatarPath = sharedPref.getString("USER_AVATAR", null)
        if (avatarPath != null) {
            imgAvatar.setImageURI(Uri.parse(avatarPath))
        }

        // --- 3. LOGIC HIỂN THỊ NÚT ADMIN ---
        // Nếu vai trò là Admin thì hiện nút Phê duyệt, ngược lại thì ẩn đi
        if (currentRole == "Admin") {
            btnAdminApprove.visibility = View.VISIBLE
        } else {
            btnAdminApprove.visibility = View.GONE
        }

        // --- 4. XỬ LÝ SỰ KIỆN CLICK ---

        // Chuyển qua màn hình AdminApproveActivity
        btnAdminApprove.setOnClickListener {
            val intent = Intent(this, AdminApproveActivity::class.java)
            startActivity(intent)
        }

        btnEdit.setOnClickListener {
            val intent = Intent(this, UserDetailActivity::class.java)
            startForResult.launch(intent)
        }

        btnOrderHistory.setOnClickListener {
            // Chuyển sang màn hình lịch sử đơn hàng nếu có
            Toast.makeText(this, "Đang mở lịch sử đơn hàng", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            val editor = sharedPref.edit()
            editor.clear()
            editor.apply()
            Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnBack.setOnClickListener { finish() }
    }
}