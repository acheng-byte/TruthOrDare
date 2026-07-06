package com.truthordare.app.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.truthordare.app.data.model.CardLibrary

@Dao
interface CardLibraryDao {
    @Query("SELECT * FROM card_libraries ORDER BY isUserDefault DESC, createdAt ASC")
    fun getAllLibraries(): LiveData<List<CardLibrary>>

    @Query("SELECT * FROM card_libraries ORDER BY isUserDefault DESC, createdAt ASC")
    suspend fun getAllLibrariesOnce(): List<CardLibrary>

    @Query("SELECT * FROM card_libraries WHERE id = :id")
    suspend fun getById(id: Long): CardLibrary?

    @Query("SELECT * FROM card_libraries WHERE isUserDefault = 1 LIMIT 1")
    suspend fun getDefaultLibrary(): CardLibrary?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(library: CardLibrary): Long

    @Update
    suspend fun update(library: CardLibrary)

    @Delete
    suspend fun delete(library: CardLibrary)

    @Query("UPDATE card_libraries SET isUserDefault = 0")
    suspend fun clearAllDefaults()

    @Query("UPDATE card_libraries SET isUserDefault = 1 WHERE id = :id")
    suspend fun setAsDefault(id: Long)
}
