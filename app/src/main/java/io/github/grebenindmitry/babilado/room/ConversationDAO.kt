package io.github.grebenindmitry.babilado.room

import androidx.room.*
import io.github.grebenindmitry.babilado.structures.Conversation

@Dao
interface ConversationDAO {
    @Query("SELECT * FROM Conversation")
    fun getAll(): List<Conversation>

    @Query("SELECT * FROM Conversation WHERE user_id=:id")
    fun getConversation(id: String): Conversation

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConversations(users: List<Conversation>)

    @Delete
    fun delete(user: Conversation)
}