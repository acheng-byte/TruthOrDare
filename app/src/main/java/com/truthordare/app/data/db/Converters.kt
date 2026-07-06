package com.truthordare.app.data.db

import androidx.room.TypeConverter
import com.truthordare.app.data.model.CardTag
import com.truthordare.app.data.model.CardType

class Converters {
    @TypeConverter fun fromCardType(value: CardType): String = value.name
    @TypeConverter fun toCardType(value: String): CardType = CardType.valueOf(value)

    @TypeConverter fun fromCardTag(value: CardTag): String = value.name
    @TypeConverter fun toCardTag(value: String): CardTag = CardTag.valueOf(value)
}
