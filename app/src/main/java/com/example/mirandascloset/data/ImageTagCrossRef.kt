package com.example.mirandascloset.data

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["imageId", "tagId"])
data class ImageTagCrossRef(
    val imageId: Long,
    @ColumnInfo(index = true)
    val tagId: Long
)