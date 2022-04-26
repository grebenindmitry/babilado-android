package io.github.grebenindmitry.babilado.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.grebenindmitry.babilado.structures.Conversation
import io.github.grebenindmitry.babilado.structures.Message
import io.github.grebenindmitry.babilado.structures.User

@Database(entities = [Message::class, User::class, Conversation::class], version = 1)
abstract class BabiladoDatabase : RoomDatabase() {
    abstract fun messageDAO(): MessageDAO
    abstract fun conversationDao(): ConversationDAO

    companion object {
        @Volatile
        private var instance: BabiladoDatabase? = null

        fun getInstance(context: Context): BabiladoDatabase {
            return instance ?: synchronized(this) { instance ?: buildDatabase(context).also { instance = it } }
        }

        private fun buildDatabase(context: Context): BabiladoDatabase {
            return Room.databaseBuilder(context, BabiladoDatabase::class.java, "babilado-database").build()
        }
    }
}