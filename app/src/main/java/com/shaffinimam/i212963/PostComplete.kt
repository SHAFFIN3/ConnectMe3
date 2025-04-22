package com.shaffinimam.i212963

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

class PostComplete : AppCompatActivity() {

    companion object {
        const val BASE_URL = "http://192.168.18.18/connectme"
    }

    private lateinit var imageView: ImageView
    private lateinit var captionInput: EditText
    private lateinit var shareButton: Button
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_post_complete)

        imageView = findViewById(R.id.finalPostImage)
        captionInput = findViewById(R.id.captionEditText)
        shareButton = findViewById(R.id.shareButton)

        val buttonC = findViewById<ImageButton>(R.id.closebutt)
        buttonC.setOnClickListener {
            val intent = Intent(this, Navigation::class.java)
            startActivity(intent)
        }

        // Show the selected image
        PostCamera.tempImageUri?.let {
            imageUri = it
            imageView.setImageURI(it)
        }

        // Handle share button
        shareButton.setOnClickListener {
            if (imageUri != null && captionInput.text.isNotBlank()) {
                uploadPost(imageUri!!, captionInput.text.toString())
            } else {
                Toast.makeText(this, "Caption or image is missing", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadPost(imageUri: Uri, caption: String) {
        thread {
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val compressedBitmap = compressBitmap(bitmap, 70) // Compress image to 70% quality
                val base64Image = bitmapToBase64(compressedBitmap)

                val userId = SharedPrefManager.getUserId(this)
                val url = URL("$BASE_URL/Post/createpost.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val postData =
                    "id=$userId&picture=${Base64.encodeToString(base64Image.toByteArray(), Base64.NO_WRAP)}&caption=${caption}"
                conn.outputStream.write(postData.toByteArray())

                val responseCode = conn.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = conn.inputStream.bufferedReader().readText()
                    val jsonResponse = org.json.JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        runOnUiThread {
                            Toast.makeText(this@PostComplete, "Post uploaded successfully", Toast.LENGTH_SHORT).show()

                            // Redirect to Navigation or your preferred activity
                            val intent = Intent(this@PostComplete, Navigation::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@PostComplete, "Failed: ${jsonResponse.getString("message")}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@PostComplete, "Server error: $responseCode", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@PostComplete, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun compressBitmap(bitmap: Bitmap, quality: Int): Bitmap {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        val byteArray = stream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
