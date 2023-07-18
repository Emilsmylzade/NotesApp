package com.example.notesapp

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.notesapp.databinding.ActivityNotesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class NotesActivity : AppCompatActivity() {
    private lateinit var noteDao: NoteDao
    private var binding: ActivityNotesBinding? = null
    private var noteId: Long = -1 // Initialize noteId with a default value


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val notesApp = application as NotesApp
        noteDao = notesApp.db.noteDao()

        binding?.etTitle?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    val lastCharIndex = s.length - 1
                    val lastChar = s[lastCharIndex].toString()

                    if (lastChar == "\n") {
                        val newlineCount = s.count { it == '\n' }

                        if (newlineCount == 1) {
                            binding?.etTitle?.setText(s.delete(lastCharIndex, lastCharIndex + 1))
                            binding?.etContent?.requestFocus()
                        }
                    }
                }
            }
        })

        binding?.ivBack?.setOnClickListener {
            updateNote()
        }

        binding?.ivDots?.setOnClickListener{
            handlingDialog()
        }
        // Retrieve the noteId from the intent extras
        noteId = intent.getLongExtra("noteId", -1)

        // Load the note details
        loadNoteDetails()
    }


    private fun handlingDialog(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dots_dialog)

        // Calculate dialog position
        val window: Window? = dialog.window
        window?.apply {
            setGravity(Gravity.TOP or Gravity.END) // Position at the top-right corner
            attributes.width = WindowManager.LayoutParams.WRAP_CONTENT
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT

            // Set margins to adjust position if needed
            val topMargin = 200
            val endMargin = 20
            attributes.x = endMargin
            attributes.y = topMargin

            setBackgroundDrawableResource(R.drawable.dialog_corners)
        }
        val refreshButton = dialog.findViewById<LinearLayout>(R.id.first_row)
        refreshButton.setOnClickListener{
            binding?.etTitle?.setText("")
            binding?.etContent?.setText("")
            dialog.dismiss()
        }

        val deleteButton = dialog.findViewById<LinearLayout>(R.id.third_row)
        deleteButton.setOnClickListener{
            deleteNote()
            dialog.dismiss()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        dialog.show()
    }
    private fun deleteNote() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val note = noteDao.getNoteById(noteId)
                withContext(Dispatchers.Main) {
                    noteDao.delete(note!!)
                }
            }
        }
    }

    private fun loadNoteDetails() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val note = noteDao.getNoteById(noteId)
                withContext(Dispatchers.Main) {
                    note?.let {
                        binding?.etTitle?.setText(it.title)
                        binding?.etContent?.setText(it.content)
                    }
                }
            }
        }
    }


    private fun updateNote() {
        val title = binding?.etTitle?.text.toString()
        val content = binding?.etContent?.text.toString()

        if (title.isNotEmpty()) {
            val currentDate = Date()
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            val formattedDate = dateFormat.format(currentDate)

            val updatedNote = NotesEntity(
                id = noteId,
                title = title,
                content = content,
                date = formattedDate
            )

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val updatedRows = noteDao.update(updatedNote) // Retrieve the number of updated rows
                    withContext(Dispatchers.Main) {
                        if (updatedRows > 0) {
                            Toast.makeText(this@NotesActivity, "Note updated successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@NotesActivity, "Failed to update note", Toast.LENGTH_SHORT).show()
                        }
                        val intent = Intent(this@NotesActivity, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
