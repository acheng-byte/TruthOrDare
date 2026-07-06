package com.truthordare.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CardType(val label: String) {
    TRUTH("真心话"),
    DARE("大冒险"),
    PROP("道具卡")
}

enum class CardTag(val label: String) {
    COUPLE("情侣"),
    DUO("双人"),
    PARTY("多人"),
    ALL("全部")
}

@Entity(
    tableName = "cards",
    foreignKeys = [ForeignKey(
        entity = CardLibrary::class,
        parentColumns = ["id"],
        childColumns = ["libraryId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("libraryId")]
)
data class Card(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val libraryId: Long,
    val type: CardType = CardType.TRUTH,
    val content: String,
    val level: Int = 1,      // 1~5
    val tag: CardTag = CardTag.ALL,
    val createdAt: Long = System.currentTimeMillis()
)
