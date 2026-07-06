package com.truthordare.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.truthordare.app.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CardLibrary::class, Card::class, GameSession::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardLibraryDao(): CardLibraryDao
    abstract fun cardDao(): CardDao
    abstract fun gameSessionDao(): GameSessionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "truthordare.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { seedDefaultData(it) }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDefaultData(db: AppDatabase) {
            val libraryDao = db.cardLibraryDao()
            val cardDao = db.cardDao()

            val libraries = listOf(
                CardLibrary(name = "热恋禁区", emoji = "❤️", description = "专为情侣二人设计，Lv.3~Lv.5 占比高", isDefault = true),
                CardLibrary(name = "双人互损", emoji = "🤪", description = "适合好基友，Lv.1~Lv.3 恶搞向", isDefault = true),
                CardLibrary(name = "多人派对", emoji = "🎉", description = "适合3~20人聚会，含道具卡和团体惩罚", isDefault = true),
                CardLibrary(name = "核爆盲盒", emoji = "🌶️", description = "极度劲爆，Lv.5 占 80%", isDefault = true, isLocked = true)
            )

            val ids = libraries.map { libraryDao.insert(it) }

            // 热恋禁区 cards
            val coupleCards = listOf(
                Card(libraryId = ids[0], type = CardType.TRUTH, content = "你有没有曾经喜欢过我以外的人？", level = 3, tag = CardTag.COUPLE),
                Card(libraryId = ids[0], type = CardType.TRUTH, content = "我们在一起最让你感动的一刻是什么？", level = 2, tag = CardTag.COUPLE),
                Card(libraryId = ids[0], type = CardType.TRUTH, content = "你上次偷偷看别人是什么时候？", level = 3, tag = CardTag.COUPLE),
                Card(libraryId = ids[0], type = CardType.TRUTH, content = "你最想对我说但一直没说的话是什么？", level = 4, tag = CardTag.COUPLE),
                Card(libraryId = ids[0], type = CardType.TRUTH, content = "你心里有没有从没告诉我的秘密？", level = 4, tag = CardTag.COUPLE),
                Card(libraryId = ids[0], type = CardType.DARE, content = "用10个字表白我一次", level = 2, tag = CardTag.COUPLE),
                Card(libraryId = ids[0], type = CardType.DARE, content = "给我一个你最想给的吻", level = 4, tag = CardTag.COUPLE),
                Card(libraryId = ids[0], type = CardType.DARE, content = "模仿我说话30秒", level = 2, tag = CardTag.COUPLE),
                Card(libraryId = ids[0], type = CardType.DARE, content = "告诉我三件你爱我的理由", level = 3, tag = CardTag.COUPLE),
                Card(libraryId = ids[0], type = CardType.DARE, content = "把我的手机解锁密码说出来，或者改成我要求的", level = 5, tag = CardTag.COUPLE),
            )

            // 双人互损 cards
            val duoCards = listOf(
                Card(libraryId = ids[1], type = CardType.TRUTH, content = "你觉得我最令你烦恼的一个习惯是什么？", level = 2, tag = CardTag.DUO),
                Card(libraryId = ids[1], type = CardType.TRUTH, content = "你有没有在背后说过我坏话？", level = 3, tag = CardTag.DUO),
                Card(libraryId = ids[1], type = CardType.TRUTH, content = "你觉得我最大的缺点是什么？", level = 2, tag = CardTag.DUO),
                Card(libraryId = ids[1], type = CardType.DARE, content = "用最夸张的动作表演你对我的印象", level = 1, tag = CardTag.DUO),
                Card(libraryId = ids[1], type = CardType.DARE, content = "发一条让人尴尬的朋友圈，集齐5个赞才能删", level = 3, tag = CardTag.DUO),
                Card(libraryId = ids[1], type = CardType.DARE, content = "学狗叫3声，不能笑场", level = 2, tag = CardTag.DUO),
                Card(libraryId = ids[1], type = CardType.DARE, content = "用筷子夹10粒豆子，30秒内完成", level = 1, tag = CardTag.DUO),
            )

            // 多人派对 cards
            val partyCards = listOf(
                Card(libraryId = ids[2], type = CardType.TRUTH, content = "在场的人中你最不想和谁喝酒？", level = 2, tag = CardTag.PARTY),
                Card(libraryId = ids[2], type = CardType.TRUTH, content = "你觉得在座的谁最有可能先结婚？", level = 1, tag = CardTag.PARTY),
                Card(libraryId = ids[2], type = CardType.TRUTH, content = "你有没有在聚会上假装喝醉过？", level = 2, tag = CardTag.PARTY),
                Card(libraryId = ids[2], type = CardType.DARE, content = "模仿在场任意一人，让大家猜是谁", level = 1, tag = CardTag.PARTY),
                Card(libraryId = ids[2], type = CardType.DARE, content = "给在场所有人每人说一个优点", level = 1, tag = CardTag.PARTY),
                Card(libraryId = ids[2], type = CardType.DARE, content = "和在场最高的人比腕力", level = 1, tag = CardTag.PARTY),
                Card(libraryId = ids[2], type = CardType.PROP, content = "道具牌：全体举杯，喝掉手中饮料的一半！", level = 2, tag = CardTag.PARTY),
                Card(libraryId = ids[2], type = CardType.PROP, content = "道具牌：顺时针每人说一个\"从没做过的事\"，撒谎就罚", level = 2, tag = CardTag.PARTY),
            )

            // 核爆盲盒 cards
            val nukeCards = listOf(
                Card(libraryId = ids[3], type = CardType.TRUTH, content = "你曾经做过的最不道德的事是什么？", level = 5, tag = CardTag.ALL),
                Card(libraryId = ids[3], type = CardType.TRUTH, content = "你最不敢让父母知道的一件事是什么？", level = 5, tag = CardTag.ALL),
                Card(libraryId = ids[3], type = CardType.DARE, content = "把手机最近的聊天记录截图给大家看", level = 5, tag = CardTag.ALL),
                Card(libraryId = ids[3], type = CardType.DARE, content = "大声念出手机里最尴尬的一条消息", level = 5, tag = CardTag.ALL),
            )

            cardDao.insertAll(coupleCards + duoCards + partyCards + nukeCards)
        }
    }
}
