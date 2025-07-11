package com.example.mirandascloset.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ImageWithTags(
    @Embedded val image: ImageEntity,
    @Relation(
        parentColumn = "imageId",
        entityColumn = "tagId",
        associateBy = Junction(ImageTagCrossRef::class)
    )
    val tags: List<TagEntity>
)