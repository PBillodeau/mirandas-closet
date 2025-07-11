package com.example.mirandascloset.data

import android.content.Context
import android.graphics.Bitmap
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

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
    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAllTags(): List<TagEntity>

    @Transaction
    @Query("SELECT * FROM images WHERE imageId = :imageId")
    suspend fun getImageById(imageId: Long): ImageWithTags?

    @Transaction
    @Query("SELECT * FROM tags WHERE name = :tagName")
    suspend fun getTagByName(tagName: String): TagEntity?

    @Transaction
    @Query("SELECT * FROM tags t where exists (SELECT 1 FROM ImageTagCrossRef r WHERE r.tagId = t.tagId) ORDER BY name ASC")
    fun getAllTagsWithImages(): Flow<List<TagWithImages>>

    @Query("DELETE FROM ImageTagCrossRef WHERE imageId = :imageId")
    suspend fun deleteImageTags(imageId: Long)

    @Query("DELETE FROM tags WHERE tagId = :tagId")
    suspend fun deleteTagById(tagId: Long)

    @Query("DELETE FROM images WHERE imageId = :imageId")
    suspend fun deleteImageById(imageId: Long)

    suspend fun deleteImage(imageWithTags: ImageWithTags) {
        this.deleteImageById(imageWithTags.image.imageId)
        this.deleteImageTags(imageWithTags.image.imageId)
        val file = File(imageWithTags.image.filePath)
        if (file.exists()) file.delete()
    }

    suspend fun createImage(context: Context, photoBitmap: Bitmap, tags: String) {
        val filename = "IMG_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            photoBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }

        val imageId = this.insertImage(ImageEntity(filePath = file.absolutePath))

        this.updateImageTags(imageId, tags)
    }

    suspend fun updateImageTags(imageId: Long, tags: String)
    {
        val tagsList = tags.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        this.deleteImageTags(imageId)
        for (tagName in tagsList) {
            var tagId = this.getTagByName(tagName)?.tagId
            if (tagId == null) {
                tagId = this.insertTag(TagEntity(name = tagName))
            }
            this.insertImageTagCrossRef(ImageTagCrossRef(imageId, tagId))
        }
    }
}