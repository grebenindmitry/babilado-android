package io.github.grebenindmitry.babilado.structures

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Message(
    @PrimaryKey val id: String,
    val sender: String,
    val recipient: String,
    @SerialName("msg_data") val data: String,
    @SerialName("msg_type") val type: Int,
    val time_sent: Long) : Parcelable {
    constructor(`in`: Parcel) : this(`in`.readString()!!, `in`.readString()!!, `in`.readString()!!, `in`.readString()!!,
        `in`.readInt(), `in`.readLong())

    companion object CREATOR : Parcelable.Creator<Message?> {
        override fun createFromParcel(`in`: Parcel): Message {
            return Message(`in`)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(sender)
        dest.writeString(recipient)
        dest.writeString(data)
        dest.writeInt(type)
        dest.writeLong(time_sent)
    }
}