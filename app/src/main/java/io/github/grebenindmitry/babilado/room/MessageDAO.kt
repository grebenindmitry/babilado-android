package io.github.grebenindmitry.babilado.room

import androidx.room.*
import io.github.grebenindmitry.babilado.structures.Message

@Dao
interface MessageDAO {
    @Query("SELECT * FROM Message")
    fun getAll(): List<Message>

    @Query("SELECT * FROM Message WHERE id=:id")
    fun getMessage(id: String): Message

    @Query("SELECT * FROM Message WHERE sender=:userId OR recipient=:userId ORDER BY time_sent DESC")
    fun getMessagesByRecipient(userId: String): List<Message>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMessages(vararg messages: Message)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMessages(messages: List<Message>)

    @Delete
    fun delete(message: Message)
}