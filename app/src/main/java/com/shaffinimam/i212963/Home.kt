package com.shaffinimam.i212963

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shaffinimam.i212963.StoriesDB.StoryDbHelper
import com.shaffinimam.i212963.StoriesDB.StoryRepository
import com.shaffinimam.i212963.apiconfig.apiconf
import com.shaffinimam.i212963s.StoryAdapter

class Home : Fragment() {

    private lateinit var dbHelper: StoryDbHelper
    private lateinit var adapter: StoryAdapter
    private lateinit var rvStories: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = StoryDbHelper(requireContext())

        // Kick off the network + DB sync
        val apiUrl = "${apiconf.BASE_URL}Story/getstories.php"
        StoryRepository.syncStories(
            requireContext(),
            apiUrl,
            onSuccess = {
                // Volley callbacks are already on main thread, but safe to do this:
                activity?.runOnUiThread { loadFromDb() }
            },
            onError = { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Make sure this layout file actually has a RecyclerView with id="rvStories"
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvStories = view.findViewById(R.id.rvStories)
        rvStories.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        // Load whatever’s already in the DB
        loadFromDb()
    }

    private fun loadFromDb() {
        val list = dbHelper.getAllStories()
        adapter = StoryAdapter(list) { storyPicBase64 ->
            showStoryDialog(storyPicBase64)
        }
        rvStories.adapter = adapter
    }

    private fun showStoryDialog(pictureBase64: String) {
        val bytes = Base64.decode(pictureBase64, Base64.DEFAULT)
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        AlertDialog.Builder(requireContext())
            .setView(
                ImageView(requireContext()).apply {
                    setImageBitmap(bmp)
                    adjustViewBounds = true
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    setPadding(16, 16, 16, 16)
                }
            )
            .setPositiveButton("Close", null)
            .show()
    }
}
