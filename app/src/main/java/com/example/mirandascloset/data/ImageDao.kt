package com.example.mirandascloset.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Insert
    suspend fun insertImage(image: ImageEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertImageTagCrossRef(crossRef: ImageTagCrossRef)

    @Transaction
    @Query("SELECT * FROM images ORDER BY imageId DESC")
    fun getAllImages(): Flow<List<ImageWithTags>>

    @Transaction
    @Query("SELECT * FROM tags WHERE name = :tagName")
    suspend fun getTagByName(tagName: String): TagEntity?

    @Transaction
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTagsWithImages(): Flow<List<TagWithImages>>
}