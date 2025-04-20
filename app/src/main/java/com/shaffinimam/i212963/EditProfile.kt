package com.shaffinimam.i212963

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.shaffinimam.i212963.apiconfig.apiconf
import java.io.ByteArrayOutputStream

class EditProfile : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private var base64Image: String? = null
    private var userID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        userID = SharedPrefManager.getUserId(this)
        imageView = findViewById(R.id.prfpic)
        val saveBtn = findViewById<TextView>(R.id.save)

        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 100)
        }

        saveBtn.setOnClickListener {
            sendProfileToServer()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            imageView.setImageURI(imageUri)

            val inputStream = contentResolver.openInputStream(imageUri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            base64Image = encodeImageToBase64(bitmap)
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val imageBytes = outputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun sendProfileToServer() {
        // Get data from EditText fields
        val username = findViewById<EditText>(R.id.usrname).text.toString()
        val contact = findViewById<EditText>(R.id.contact).text.toString()
        val bio = findViewById<EditText>(R.id.bio).text.toString()
        val picture = base64Image ?: ""


        val url = apiconf.BASE_URL + "profile/editprofile.php"  // or your actual file name

        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                Log.e("E", "sendProfileToServer: $response")
                Toast.makeText(this, "Server: $response", Toast.LENGTH_LONG).show()
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = userID.toString()  // Use the user ID from shared preferences
                params["username"] = username
                params["contact"] = contact
                params["bio"] = bio
                params["picture"] = picture
                return params
            }
        }

        requestQueue.add(stringRequest)
    }


}