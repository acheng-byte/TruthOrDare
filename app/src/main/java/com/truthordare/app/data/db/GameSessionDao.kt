package com.truthordare.app.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.truthordare.app.data.model.GameSession

@Dao
interface GameSessionDao {
    @Query("SELECT * FROM game_sessions WHERE savedAt > 0 ORDER BY savedAt DESC")
    fun getSavedSessions(): LiveData<List<GameSession>>

    @Query("SELECT * FROM game_sessions WHERE savedAt > 0 AND libraryName LIKE '%' || :query || '%' ORDER BY savedAt DESC")
    fun searchSessions(query: String): LiveData<List<GameSession>>

    @Query("SELECT * FROM game_sessions WHERE savedAt > 0 AND savedAt BETWEEN :from AND :to ORDER BY savedAt DESC")
    fun getSessionsByDateRange(from: Long, to: Long): LiveData<List<GameSession>>

    @Query("SELECT * FROM game_sessions WHERE id = :id")
    suspend fun getById(id: Long): GameSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: GameSession): Long

    @Update
    suspend fun update(session: GameSession)

    @Delete
    suspend fun delete(session: GameSession)

    @Query("DELETE FROM game_sessions WHERE savedAt > 0")
    suspend fun deleteAllSaved()
}
