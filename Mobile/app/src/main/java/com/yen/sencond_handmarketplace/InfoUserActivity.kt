package com.yen.sencond_handmarketplace

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class InfoUserActivity : AppCompatActivity() {

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data

            // 1. Nhận dữ liệu
            val newName = data?.getStringExtra("NEW_NAME")
            val newContact = data?.getStringExtra("NEW_CONTACT")
            val newAddress = data?.getStringExtra("NEW_ADDRESS")
            val newAvatarUri = data?.getStringExtra("NEW_AVATAR")

            // 2. Ánh xạ View
            val txtName = findViewById<TextView>(R.id.txtUserName)
            val txtContact = findViewById<TextView>(R.id.txtUserContact) // Bạn cần đảm bảo ID này có trong XML
            val txtAddress = findViewById<TextView>(R.id.txtUserAddress)
            val imgAvatar = findViewById<ImageView>(R.id.imgUserAvatar)

            // 3. Cập nhật giao diện & Lưu vào SharedPreferences
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()

            if (!newName.isNullOrEmpty()) {
                txtName.text = newName
                editor.putString("USER_NAME", newName)
            }

            if (!newContact.isNullOrEmpty()) {
                txtContact.text = newContact
                editor.putString("USER_CONTACT", newContact)
            }

            if (!newAddress.isNullOrEmpty()) {
                txtAddress.text = newAddress
                editor.putString("USER_ADDRESS", newAddress)
            }

            if (!newAvatarUri.isNullOrEmpty()) {
                imgAvatar.setImageURI(Uri.parse(newAvatarUri))
                editor.putString("USER_AVATAR", newAvatarUri)
            }

            editor.apply() // Chốt lưu dữ liệu
            Toast.makeText(this, "Đã cập nhật thông tin!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_info_user)

        // Ánh xạ View trong onCreate
        val txtName = findViewById<TextView>(R.id.txtUserName)
        val txtContact = findViewById<TextView>(R.id.txtUserContact)
        val txtAddress = findViewById<TextView>(R.id.txtUserAddress)
        val imgAvatar = findViewById<ImageView>(R.id.imgUserAvatar)

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnBack = findViewById<ImageButton>(R.id.btnBackProfile)
        val btnEdit = findViewById<ImageButton>(R.id.btnEditProfile)

        // 4. HIỂN THỊ DỮ LIỆU KHI MỞ MÀN HÌNH
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        txtName.text = sharedPref.getString("USER_NAME", "Yen")
        txtContact.text = sharedPref.getString("USER_CONTACT", "Chưa có liên hệ")
        txtAddress.text = sharedPref.getString("USER_ADDRESS", "Chưa có địa chỉ")

        val avatarPath = sharedPref.getString("USER_AVATAR", null)
        if (avatarPath != null) {
            imgAvatar.setImageURI(Uri.parse(avatarPath))
        }

        // Logic nút bấm giữ nguyên...
        btnEdit.setOnClickListener {
            val intent = Intent(this, UserDetailActivity::class.java)
            startForResult.launch(intent)
        }

        // Nút Logout và Back giữ nguyên như code cũ của bạn
    }
}