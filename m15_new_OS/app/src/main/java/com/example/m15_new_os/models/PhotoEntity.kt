package com.example.m15_new_os.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "photo_uri") val photoUri: String,
    @ColumnInfo(name = "date_taken") val dateTaken: Long
)
