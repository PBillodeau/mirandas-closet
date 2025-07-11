package com.example.mirandascloset.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TagWithImages(
    @Embedded val tag: TagEntity,
    @Relation(
        parentColumn = "tagId",
        entityColumn = "imageId",
        associateBy = Junction(ImageTagCrossRef::class)
    )
    val images: List<ImageEntity>
)