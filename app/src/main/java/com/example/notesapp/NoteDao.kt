package com.example.notesapp


import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes")
    fun getAllNotes(): LiveData<List<NotesEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Long): NotesEntity?

    @Insert
    suspend fun insert(note: NotesEntity): Long

    @Update
    suspend fun update(note: NotesEntity):Int

    @Delete
    suspend fun delete(note: NotesEntity)
}
