package com.truthordare.app.data.repository

import android.content.Context
import com.truthordare.app.data.db.AppDatabase
import com.truthordare.app.data.model.DrawnCardRecord
import com.truthordare.app.data.model.GameSession
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SessionRepository(context: Context) {
    private val dao = AppDatabase.getInstance(context).gameSessionDao()
    private val gson = Gson()

    val savedSessions = dao.getSavedSessions()

    fun searchSessions(query: String) = dao.searchSessions(query)

    fun getSessionsByDateRange(from: Long, to: Long) = dao.getSessionsByDateRange(from, to)

    suspend fun startSession(libraryId: Long, libraryName: String): Long {
        return dao.insert(GameSession(libraryId = libraryId, libraryName = libraryName))
    }

    suspend fun saveSession(sessionId: Long, memo: String, records: List<DrawnCardRecord>) {
        val session = dao.getById(sessionId) ?: return
        val maxLevel = records.maxOfOrNull { it.level } ?: 1
        dao.update(
            session.copy(
                memo = memo,
                totalDraws = records.size,
                maxLevel = maxLevel,
                drawDetails = gson.toJson(records),
                savedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getRecords(sessionId: Long): List<DrawnCardRecord> {
        val session = dao.getById(sessionId) ?: return emptyList()
        val type = object : TypeToken<List<DrawnCardRecord>>() {}.type
        return gson.fromJson(session.drawDetails, type) ?: emptyList()
    }

    suspend fun deleteSession(session: GameSession) = dao.delete(session)

    suspend fun clearAllHistory() = dao.deleteAllSaved()
}
