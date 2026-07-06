package com.truthordare.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "card_libraries")
data class CardLibrary(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "🎲",
    val description: String = "",
    val isDefault: Boolean = false,
    val isLocked: Boolean = false,   // e.g. 核爆盲盒 requires unlock
    val isUserDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
