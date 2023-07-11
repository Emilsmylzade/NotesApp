package com.example.notesapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date


class MainActivity : AppCompatActivity() {
    private lateinit var noteDao: NoteDao
    private lateinit var notesAdapter: NotesAdapter
    private var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val layoutManager = LinearLayoutManager(this)
        binding?.rvNotes?.layoutManager = layoutManager

        notesAdapter = NotesAdapter(emptyList()) // Pass an empty list initially
        binding?.rvNotes?.adapter = notesAdapter

        val notesApp = application as NotesApp
        noteDao = notesApp.db.noteDao()

        noteDao.getAllNotes().observe(this) { notes ->
            notes?.let {
                notesAdapter.updateData(it)
            }
            notesAdapter.notifyDataSetChanged()
        }

        val gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }
        })

        binding?.rvNotes?.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val childView = rv.findChildViewUnder(e.x, e.y)
                if (childView != null && gestureDetector.onTouchEvent(e)) {
                    val position = rv.getChildAdapterPosition(childView)
                    val selectedNote = notesAdapter.getItemAtPosition(position)
                    noteDao.getAllNotes().observe(this@MainActivity) { notes ->
                        notes?.let {
                            notesAdapter.updateANote(selectedNote)
                        }
                        notesAdapter.notifyDataSetChanged()
                    }
                    val intent = Intent(this@MainActivity, NotesActivity::class.java)
                    intent.putExtra("noteId", selectedNote.id) // Pass the noteId to the NotesActivity
                    startActivity(intent)
                    return true
                }
                return false
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })


        binding?.ivAdd?.setOnClickListener{
            addNewNote()
        }


    }
    private fun addNewNote() {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)

        val newNote = NotesEntity(
            title = "New Note",
            content = "",
            date = formattedDate
        )

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val insertedNoteId = noteDao.insert(newNote) // Retrieve the ID of the inserted note
                val updatedNotesList = noteDao.getAllNotes().value // Get the updated list of notes
                withContext(Dispatchers.Main) {
                    updatedNotesList?.let {
                        notesAdapter.updateData(it) // Update the adapter with the new data
                    }
                    notesAdapter.notifyDataSetChanged()
                    val intent = Intent(this@MainActivity, NotesActivity::class.java)
                    intent.putExtra("noteId", insertedNoteId) // Pass the ID of the inserted note
                    startActivity(intent)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}