package io.github.grebenindmitry.babilado.structures

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Conversation(
    @PrimaryKey @Embedded(prefix = "user_") val user: User, @Embedded(prefix = "message_") val lastMsg: Message) :
    Parcelable {
    constructor(`in`: Parcel) : this(`in`.readTypedObject(User.CREATOR)!!, `in`.readTypedObject(Message.CREATOR)!!)

    companion object CREATOR : Parcelable.Creator<Conversation?> {
        override fun createFromParcel(`in`: Parcel): Conversation {
            return Conversation(`in`)
        }

        override fun newArray(size: Int): Array<Conversation?> {
            return arrayOfNulls(size)
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeTypedObject(user, 0)
        dest.writeTypedObject(lastMsg, 0)
    }
}
