package com.shaffinimam.i212963

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageButton
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.shaffinimam.i212963.apiconfig.apiconf
import com.shaffinimam.i212963.profiledb.ProfileDBHelper
import de.hdodenhof.circleimageview.CircleImageView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import com.android.volley.Request.Method

class DM2 : AppCompatActivity() {

    private var id = 0
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessagesAdapter
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dm2)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        id = intent.getIntExtra("id", -1)
        Log.d("DM2", "Received receiver ID: $id")

        var base64 = ""
        var name = ""

        if (id != -1) {
            val db = ProfileDBHelper(this)
            val user = db.getProfileById(id)
            if (user != null) {
                name = user.username
                base64 = user.picture
            }
        }

        toolbar.title = name

        val bitmap = if (base64.isNotEmpty()) {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            null
        }

        val prf = findViewById<CircleImageView>(R.id.prfp)
        prf.setImageBitmap(bitmap)

        val cal = findViewById<ImageButton>(R.id.callpers)
        cal.setOnClickListener {
            val intent = Intent(this, Call::class.java)
            intent.putExtra("name", name)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.messages_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessagesAdapter(messages)
        recyclerView.adapter = messageAdapter

        loadMessages()

        val sendBtn = findViewById<ImageButton>(R.id.send_button)
        val messageInput = findViewById<EditText>(R.id.message_input)

        sendBtn.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageInput.text.clear()
            } else {
                Log.d("DM2", "Message input is empty.")
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish() // Close current activity
        return true
    }

    private fun sendMessage(message: String) {
        val senderId = SharedPrefManager.getUserId(this)

        if (senderId == -1) {
            Log.e("DM2", "Error: User is not authenticated. sender_id is -1")
            return
        }

        val url = "${apiconf.BASE_URL}/Message/send_message.php"
        Log.d("DM2", "Sending message from sender $senderId to receiver $id: $message")

        val queue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                Log.d("DM2", "Message sent successfully, response: $response")
                loadMessages()
            },
            Response.ErrorListener { error ->
                Log.e("DM2", "Error sending message: ${error.message}")
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["sender_id"] = senderId.toString()
                params["receiver_id"] = id.toString()
                params["message_type"] = "text"
                params["message_content"] = message
                return params
            }
        }
        queue.add(stringRequest)
    }

    private fun loadMessages() {
        val senderId = SharedPrefManager.getUserId(this)

        if (senderId == -1) {
            Log.e("DM2", "Error: User is not authenticated. sender_id is -1")
            return
        }

        val url = "${apiconf.BASE_URL}/Message/get_messages.php"
        Log.d("DM2", "Loading messages for sender $senderId and receiver $id")

        val queue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                Log.d("DM2", "Messages loaded: $response")
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "failure") {
                        Log.e("DM2", "Error loading messages: ${jsonResponse.getString("message")}")
                        return@Listener
                    }

                    val messagesList = parseMessages(response)
                    messages.clear()
                    messages.addAll(messagesList)
                    messageAdapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    Log.e("DM2", "Error parsing messages: ${e.message}")
                }
            },
            Response.ErrorListener { error ->
                Log.e("DM2", "Error loading messages: ${error.message}")
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user1"] = senderId.toString()
                params["user2"] = id.toString()
                return params
            }
        }
        queue.add(stringRequest)
    }

    private fun parseMessages(response: String): List<Message> {
        val messages = mutableListOf<Message>()

        try {
            val jsonResponse = JSONObject(response)
            if (jsonResponse.getString("status") == "failure") {
                Log.e("DM2", "Error: ${jsonResponse.getString("message")}")
                return messages
            }

            val jsonArray = jsonResponse.getJSONArray("messages")
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val senderId = jsonObject.getInt("sender_id")
                val receiverId = jsonObject.getInt("receiver_id")
                val text = jsonObject.getString("message_content")
                val messageType = jsonObject.getString("message_type")
                val timestamp = jsonObject.getString("timestamp")

                val message = Message(senderId, receiverId, text, messageType, timestamp)
                messages.add(message)
            }
        } catch (e: Exception) {
            Log.e("DM2", "Error parsing messages: ${e.message}")
        }

        return messages
    }
}
