package io.github.grebenindmitry.babilado.structures

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class User(
    @PrimaryKey val id: String, val username: String) : Parcelable {
    constructor(`in`: Parcel) : this(`in`.readString()!!, `in`.readString()!!)

    companion object CREATOR : Parcelable.Creator<User?> {
        override fun createFromParcel(`in`: Parcel): User {
            return User(`in`)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(username)
    }
}