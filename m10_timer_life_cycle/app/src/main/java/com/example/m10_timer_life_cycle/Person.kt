package com.example.m10_timer_life_cycle

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Person(val surName: String, val name: String, val middleName: String?) : Parcelable{
}
