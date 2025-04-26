package com.shaffinimam.i212963

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.shaffinimam.i212963.apiconfig.apiconf
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var callListener: CallListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId != -1) {
            Log.d(TAG, "User already logged in with ID: $userId")
            // Initialize call listener here to ensure it's created for logged-in users
            callListener = CallListener(applicationContext, userId)

            startActivity(Intent(this, Navigation::class.java))
            finish()
        } else {
            Log.d(TAG, "No user logged in, going to login screen")
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                saveTokenToServer(userId, token)
            }
        }
    }

    private fun saveTokenToServer(userId: Int, token: String) {
        val url = apiconf.BASE_URL+"save_tokken.php"
        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                Log.d("FCM", "Token saved: $response")
            },
            Response.ErrorListener { error ->
                Log.e("FCM", "Failed to save token: ${error.message}")
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId.toString()
                params["fcm_token"] = token
                return params
            }
        }

        requestQueue.add(stringRequest)
    }

}

