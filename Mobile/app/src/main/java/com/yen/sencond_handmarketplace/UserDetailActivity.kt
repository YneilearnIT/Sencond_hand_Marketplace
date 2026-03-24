package com.yen.sencond_handmarketplace

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class UserDetailActivity : AppCompatActivity() {

    private lateinit var imgAvatar: ImageView
    private lateinit var edtName: EditText
    private lateinit var edtContact: EditText
    private lateinit var edtAddress: EditText
    private var imageUri: Uri? = null // Biến lưu ảnh tạm thời

    // Bộ chọn ảnh từ máy
    private val getImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            imgAvatar.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)

        // Ánh xạ các View từ XML bạn đã gửi
        imgAvatar = findViewById(R.id.imgEditAvatar)
        edtName = findViewById(R.id.edtEditName)
        edtContact = findViewById(R.id.edtEditContact)
        edtAddress = findViewById(R.id.edtEditAddress)

        val btnBack = findViewById<ImageButton>(R.id.btnBackEdit)
        val btnDelete = findViewById<Button>(R.id.btnDeleteInfo)
        val btnSave = findViewById<Button>(R.id.btnSaveInfo)

        // Chọn ảnh khi nhấn vào avatar hoặc nút camera
        imgAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getImage.launch(intent)
        }

        // --- XỬ LÝ NÚT XÓA THÔNG TIN ---
        btnDelete.setOnClickListener {
            // Xóa sạch nội dung trong các ô nhập liệu
            edtName.setText("")
            edtContact.setText("")
            edtAddress.setText("")

            // Đưa ảnh về mặc định (nếu muốn)
            imgAvatar.setImageResource(android.R.drawable.ic_menu_gallery)
            imageUri = null

            Toast.makeText(this, "Đã xóa toàn bộ nội dung nhập!", Toast.LENGTH_SHORT).show()
        }

        // --- XỬ LÝ NÚT LƯU THÔNG TIN ---
        btnSave.setOnClickListener {
            val name = edtName.text.toString().trim()
            val contact = edtContact.text.toString().trim()
            val address = edtAddress.text.toString().trim()

            // Kiểm tra xem người dùng đã nhập tên chưa
            if (name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập họ và tên!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gửi dữ liệu quay lại màn hình InfoUserActivity
            val returnIntent = Intent()
            returnIntent.putExtra("NEW_NAME", name)
            returnIntent.putExtra("NEW_CONTACT", contact)
            returnIntent.putExtra("NEW_ADDRESS", address)
            imageUri?.let { returnIntent.putExtra("NEW_AVATAR", it.toString()) }

            setResult(Activity.RESULT_OK, returnIntent)

            // Hiển thị thông báo thành công
            Toast.makeText(this, "Lưu thông tin thành công!", Toast.LENGTH_SHORT).show()

            // Đóng màn hình này để quay lại InfoUser
            finish()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}