package com.truthordare.app.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.truthordare.app.data.model.Card
import com.truthordare.app.data.model.CardTag
import com.truthordare.app.data.model.CardType

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE libraryId = :libraryId ORDER BY createdAt ASC")
    fun getCardsForLibrary(libraryId: Long): LiveData<List<Card>>

    @Query("SELECT * FROM cards WHERE libraryId = :libraryId ORDER BY createdAt ASC")
    suspend fun getCardsForLibraryOnce(libraryId: Long): List<Card>

    @Query("""
        SELECT * FROM cards
        WHERE libraryId = :libraryId
        AND level <= :maxLevel
        AND (:tag = 'ALL' OR tag = :tag)
        ORDER BY RANDOM() LIMIT 1
    """)
    suspend fun drawRandom(libraryId: Long, maxLevel: Int, tag: String): Card?

    @Query("""
        SELECT * FROM cards
        WHERE libraryId = :libraryId
        AND level <= :maxLevel
        AND (:tag = 'ALL' OR tag = :tag)
        AND type = :type
        ORDER BY RANDOM() LIMIT 1
    """)
    suspend fun drawRandomByType(libraryId: Long, maxLevel: Int, tag: String, type: String): Card?

    @Query("SELECT COUNT(*) FROM cards WHERE libraryId = :libraryId")
    suspend fun countCardsInLibrary(libraryId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: Card): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<Card>)

    @Update
    suspend fun update(card: Card)

    @Delete
    suspend fun delete(card: Card)

    @Query("DELETE FROM cards WHERE libraryId = :libraryId")
    suspend fun deleteAllInLibrary(libraryId: Long)
}
