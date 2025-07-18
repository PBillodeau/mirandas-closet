package com.example.mirandascloset

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.mirandascloset.data.AppDatabase
import com.example.mirandascloset.data.ImageWithTags
import com.example.mirandascloset.data.TagEntity
import com.example.mirandascloset.ui.theme.MirandasClosetTheme
import java.io.File
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.layout.ContentScale


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MirandasClosetTheme {
                val db = AppDatabase.getInstance(this)
                val imageDao = db.imageDao()
                val imagesWithTagsFlow = imageDao.getAllImages()
                val tagsWithImagesFlow = imageDao.getAllTagsWithImages()
                val imagesWithTags by imagesWithTagsFlow.collectAsState(initial = emptyList())
                val tagsWithImages by tagsWithImagesFlow.collectAsState(initial = emptyList())
                var selectedTag by remember { mutableStateOf<TagEntity?>(null) }
                val context = this

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Miranda's Closet") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            )
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                val intent = Intent(this, AddPhotoActivity::class.java)
                                startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.Add, tint = MaterialTheme.colorScheme.primary, contentDescription = "Add Photo")
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TagDropdown(
                            tags = tagsWithImages.map { it.tag },
                            selectedTag = selectedTag,
                            onTagSelected = { selectedTag = it }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (imagesWithTags.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No images yet. Tap + to add one!")
                            }
                        } else {
                            val filteredImages = if (selectedTag == null) {
                                imagesWithTags
                            } else {
                                imagesWithTags.filter { imgWithTags ->
                                    imgWithTags.tags.any { it.tagId == selectedTag?.tagId }
                                }
                            }
                            ImageGrid(
                                images = filteredImages,
                                modifier = Modifier.weight(1f),
                                onImageClick = { imageWithTags ->
                                    val intent = Intent(context, EditImageActivity::class.java)
                                    intent.putExtra("imageId", imageWithTags.image.imageId)
                                    startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagDropdown(
    tags: List<TagEntity>,
    selectedTag: TagEntity?,
    onTagSelected: (TagEntity?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = selectedTag?.name ?: "All tags"
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Filter by tag") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("All tags") },
                onClick = {
                    onTagSelected(null)
                    expanded = false
                }
            )
            tags.forEach { tag ->
                DropdownMenuItem(
                    text = { Text(tag.name) },
                    onClick = {
                        onTagSelected(tag)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ImageGrid(images: List<ImageWithTags>, modifier: Modifier = Modifier, onImageClick: (ImageWithTags) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(images.size) { image ->
            Box(modifier = Modifier.clickable { onImageClick(images[image]) }) {
                ImageListItem(images[image])
            }
        }
    }
}

@Composable
fun ImageListItem(image: ImageWithTags) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imgFile = File(image.image.filePath)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Saved photo",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            image.tags.forEach { tag ->
                Text (
                    text = tag.name,
                )
            }
        }
    }
}
