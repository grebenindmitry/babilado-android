package io.github.grebenindmitry.babilado.structures

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import kotlinx.serialization.Serializable

@Serializable
@RequiresApi(Build.VERSION_CODES.M)
data class Session(val id: String, val user: User, val expiryTime: Long) : Parcelable {
    constructor(`in`: Parcel) : this(`in`.readString()!!, `in`.readTypedObject(User.CREATOR)!!, `in`.readLong())

    companion object CREATOR : Parcelable.Creator<Session?> {
        override fun createFromParcel(`in`: Parcel): Session {
            return Session(`in`)
        }

        override fun newArray(size: Int): Array<Session?> {
            return arrayOfNulls(size)
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeTypedObject(user, 0)
        dest.writeLong(expiryTime)
    }
}
