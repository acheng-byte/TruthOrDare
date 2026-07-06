package com.truthordare.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_sessions")
data class GameSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val libraryId: Long,
    val libraryName: String,
    val memo: String = "",
    val totalDraws: Int = 0,
    val maxLevel: Int = 1,
    val drawDetails: String = "[]",   // JSON array of DrawnCardRecord serialized
    val startedAt: Long = System.currentTimeMillis(),
    val savedAt: Long = 0             // 0 = not yet saved to archive
)

data class DrawnCardRecord(
    val content: String,
    val type: String,
    val level: Int,
    val playerName: String = "",
    val completed: Boolean = true,
    val drawnAt: Long = System.currentTimeMillis()
)
