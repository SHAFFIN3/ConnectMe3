package com.shaffinimam.i212963

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout


class Profile : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val button = view.findViewById<ImageButton>(R.id.editpr)
        button.setOnClickListener {
            val intent = Intent(requireContext(), EditProfile::class.java)
            startActivity(intent)
        }

        val button2 = view.findViewById<LinearLayout>(R.id.follscr)
        button2.setOnClickListener{
            val intent = Intent(requireContext(), FollowList::class.java)
            startActivity(intent)
        }

        val logout = view.findViewById<ImageButton>(R.id.logout)

        logout.setOnClickListener {
            val sharedPref = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                remove("user_id")
                apply()
            }

            val intent = Intent(requireContext(), Login::class.java)
            startActivity(intent)
            requireActivity().finish() 
        }

    }

}