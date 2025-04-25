package com.shaffinimam.i212963

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.shaffinimam.i212963.profiledb.ProfileDBHelper
import de.hdodenhof.circleimageview.CircleImageView

class DM2 : AppCompatActivity() {
    var id = 0;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dm2)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        id = intent.getIntExtra("id", -1)
        var base64 = "";
        var name = "";
        if (id != -1) {
            val db = ProfileDBHelper(this)
            val user = db.getProfileById(id) // You’ll need to implement this
            if (user != null) {
                name = user.username
                base64 = user.picture
            }
        }

        toolbar.title = name
        val bitmap = if (base64 != null) {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            null
        }

        val prf = findViewById<CircleImageView>(R.id.prfp);
        prf.setImageBitmap(bitmap)



        val cal= findViewById<ImageButton>(R.id.callpers)
        cal.setOnClickListener{
            val intent = Intent(this,Call::class.java)
            intent.putExtra("name",name)
            startActivity(intent)
        }


    }
    override fun onSupportNavigateUp(): Boolean {
        finish() // Close current activity
        return true
    }
}