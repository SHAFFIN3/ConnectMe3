package com.shaffinimam.i212963

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shaffinimam.i212963.profiledb.ProfileDBHelper
import de.hdodenhof.circleimageview.CircleImageView


class Contact_All_Fragment : Fragment() {

    private val userList = mutableListOf<Model_dm>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var dbHelper: ProfileDBHelper
    private lateinit var adapter: Adapter_cont

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = ProfileDBHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contact__all_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.rv)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = Adapter_cont(requireContext(), userList)
        recyclerView.adapter = adapter

        loadProfilesFromDB()

    }

    private fun loadProfilesFromDB() {
        userList.clear()
        val profiles = dbHelper.getAllProfiles()

        for (profile in profiles) {
            val decodedBytes = Base64.decode(profile.picture, Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            userList.add(Model_dm(profile.id,profile.username, bitmap))
        }

        adapter.notifyDataSetChanged()
    }
}


class Adapter_cont(
    private val context: Context,
    private val userList: List<Model_dm>
) : RecyclerView.Adapter<Adapter_cont.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.name)
        val profileImage: CircleImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_followers, parent, false) // your item layout
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.nameText.text = user.username
        holder.profileImage.setImageBitmap(user.pictureBitmap)

    }

    override fun getItemCount(): Int = userList.size
}

