package com.example.nittcompanion.common.objects

import android.os.Parcel
import android.os.Parcelable
import com.example.nittcompanion.common.*
import java.util.*

data class Event(val name:String,var startDate: Calendar,var endDate: Calendar,val type : String,val courceid: String = "",val alert: Boolean = false,val imp : Boolean) :
    Parcelable {
    val month : String get() = startDate.getMonthInFormat()
    val date : String get() = startDate.getDateInFormat()


    constructor(parcel: Parcel) : this (
        parcel.readString()!!,
        Calendar.getInstance().getCalEnderWithMillis(parcel.readLong()),
        Calendar.getInstance().getCalEnderWithMillis(parcel.readLong()),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeLong(startDate.timeInMillis)
        parcel.writeLong(endDate.timeInMillis)
        parcel.writeString(type)
        parcel.writeString(courceid)
        parcel.writeByte(if (alert) 1 else 0)
        parcel.writeByte(if (imp) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event {
            return Event(parcel)
        }

        override fun newArray(size: Int): Array<Event?> {
            return arrayOfNulls(size)
        }
    }

}