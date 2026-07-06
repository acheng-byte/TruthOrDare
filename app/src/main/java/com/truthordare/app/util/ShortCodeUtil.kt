package com.truthordare.app.util

import com.google.gson.Gson
import com.truthordare.app.data.model.Card
import com.truthordare.app.data.model.CardLibrary
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object ShortCodeUtil {
    private val gson = Gson()

    data class ExportPayload(
        val name: String,
        val emoji: String,
        val desc: String,
        val cards: List<CardExport>
    )

    data class CardExport(
        val c: String,   // content
        val t: String,   // type
        val l: Int,      // level
        val g: String    // tag
    )

    fun encode(library: CardLibrary, cards: List<Card>): String {
        val payload = ExportPayload(
            name = library.name,
            emoji = library.emoji,
            desc = library.description,
            cards = cards.map { CardExport(it.content, it.type.name, it.level, it.tag.name) }
        )
        val json = gson.toJson(payload)
        val compressed = gzip(json.toByteArray(Charsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(compressed)
    }

    fun decode(code: String): Pair<CardLibrary, List<Card>> {
        val compressed = Base64.getUrlDecoder().decode(code)
        val json = ungzip(compressed).toString(Charsets.UTF_8)
        val payload = gson.fromJson(json, ExportPayload::class.java)
        val library = CardLibrary(name = payload.name, emoji = payload.emoji, description = payload.desc)
        val cards = payload.cards.map { ce ->
            Card(
                libraryId = 0,
                type = com.truthordare.app.data.model.CardType.valueOf(ce.t),
                content = ce.c,
                level = ce.l,
                tag = com.truthordare.app.data.model.CardTag.valueOf(ce.g)
            )
        }
        return Pair(library, cards)
    }

    private fun gzip(data: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { it.write(data) }
        return bos.toByteArray()
    }

    private fun ungzip(data: ByteArray): ByteArray {
        return GZIPInputStream(ByteArrayInputStream(data)).use { it.readBytes() }
    }
}
