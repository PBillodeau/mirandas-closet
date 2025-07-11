package com.example.mirandascloset.data

import androidx.room.Entity

@Entity(primaryKeys = ["imageId", "tagId"])
data class ImageTagCrossRef(
    val imageId: Long,
    val tagId: Long
)