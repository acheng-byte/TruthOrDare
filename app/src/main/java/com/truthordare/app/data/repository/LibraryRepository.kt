package com.truthordare.app.data.repository

import android.content.Context
import com.truthordare.app.data.db.AppDatabase
import com.truthordare.app.data.model.Card
import com.truthordare.app.data.model.CardLibrary
import com.truthordare.app.util.ShortCodeUtil

class LibraryRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val libraryDao = db.cardLibraryDao()
    private val cardDao = db.cardDao()

    val allLibraries = libraryDao.getAllLibraries()

    suspend fun createLibrary(name: String, emoji: String = "🎲"): Long {
        return libraryDao.insert(CardLibrary(name = name, emoji = emoji))
    }

    suspend fun updateLibrary(library: CardLibrary) = libraryDao.update(library)

    suspend fun deleteLibrary(library: CardLibrary) = libraryDao.delete(library)

    suspend fun setDefaultLibrary(id: Long) {
        libraryDao.clearAllDefaults()
        libraryDao.setAsDefault(id)
    }

    suspend fun getDefaultLibraryId(): Long? = libraryDao.getDefaultLibrary()?.id

    fun getCardsForLibrary(libraryId: Long) = cardDao.getCardsForLibrary(libraryId)

    suspend fun getCardsOnce(libraryId: Long) = cardDao.getCardsForLibraryOnce(libraryId)

    suspend fun addCard(card: Card): Long = cardDao.insert(card)

    suspend fun updateCard(card: Card) = cardDao.update(card)

    suspend fun deleteCard(card: Card) = cardDao.delete(card)

    suspend fun drawCard(libraryId: Long, maxLevel: Int, tag: String) =
        cardDao.drawRandom(libraryId, maxLevel, tag)

    suspend fun drawCardByType(libraryId: Long, maxLevel: Int, tag: String, type: String) =
        cardDao.drawRandomByType(libraryId, maxLevel, tag, type)

    suspend fun exportLibraryAsShortCode(libraryId: Long): String {
        val library = libraryDao.getById(libraryId) ?: return ""
        val cards = cardDao.getCardsForLibraryOnce(libraryId)
        return ShortCodeUtil.encode(library, cards)
    }

    suspend fun importFromShortCode(code: String): Boolean {
        return try {
            val (library, cards) = ShortCodeUtil.decode(code)
            val existingLibraries = libraryDao.getAllLibrariesOnce()
            val conflict = existingLibraries.find { it.name == library.name }
            val newId = if (conflict != null) {
                cardDao.deleteAllInLibrary(conflict.id)
                conflict.id
            } else {
                libraryDao.insert(library)
            }
            cardDao.insertAll(cards.map { it.copy(libraryId = newId) })
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun importFromCsv(csv: String, libraryName: String): Boolean {
        return try {
            val lines = csv.lines().drop(1).filter { it.isNotBlank() }
            val libraryId = libraryDao.insert(CardLibrary(name = libraryName))
            val cards = lines.mapNotNull { line ->
                val parts = line.split(",")
                if (parts.size < 2) null
                else {
                    val type = when (parts.getOrNull(1)?.trim()) {
                        "大冒险" -> com.truthordare.app.data.model.CardType.DARE
                        "道具卡" -> com.truthordare.app.data.model.CardType.PROP
                        else -> com.truthordare.app.data.model.CardType.TRUTH
                    }
                    val level = parts.getOrNull(2)?.trim()?.toIntOrNull()?.coerceIn(1, 5) ?: 1
                    val tagStr = parts.getOrNull(3)?.trim() ?: ""
                    val tag = com.truthordare.app.data.model.CardTag.values()
                        .find { it.label == tagStr } ?: com.truthordare.app.data.model.CardTag.ALL
                    Card(libraryId = libraryId, type = type, content = parts[0].trim(), level = level, tag = tag)
                }
            }
            cardDao.insertAll(cards)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun copyLibrary(sourceId: Long, newName: String): Long {
        val source = libraryDao.getById(sourceId) ?: return -1
        val newId = libraryDao.insert(source.copy(id = 0, name = newName, isDefault = false))
        val cards = cardDao.getCardsForLibraryOnce(sourceId)
        cardDao.insertAll(cards.map { it.copy(id = 0, libraryId = newId) })
        return newId
    }
}
