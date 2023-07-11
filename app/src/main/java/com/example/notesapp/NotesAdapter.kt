package com.example.notesapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.databinding.ItemNotesRowBinding

class NotesAdapter(private var items: List<NotesEntity>) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemNotesRowBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = items.get(position)
        holder.tvDate.text = note.date
        holder.tvPreview.text = note.title

        if (position % 2 == 0) {
            holder.llNotesItemMain.setBackgroundColor(
                Color.parseColor("#EBEBEB")
            )
        } else {
            holder.llNotesItemMain.setBackgroundColor(
                Color.parseColor("#FFFFFF")
            )
        }
    }

    fun updateData(newItems: List<NotesEntity>) {
        items = newItems.reversed()
        notifyDataSetChanged()
    }
    fun updateANote(updatedNote: NotesEntity) {
        val updatedList = mutableListOf(updatedNote)
        updatedList.addAll(items)
        items = updatedList
        notifyDataSetChanged()
    }

    fun getItemAtPosition(position: Int): NotesEntity {
        return items[position]
    }
    class ViewHolder(private val binding: ItemNotesRowBinding) : RecyclerView.ViewHolder(binding.root) {
        val llNotesItemMain = binding.llNotesItemMain
        val tvPreview = binding.tvPreview
        val tvDate = binding.tvDate
    }
}
