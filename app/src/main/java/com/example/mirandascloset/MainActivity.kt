package com.example.mirandascloset

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mirandascloset.data.AppDatabase
import com.example.mirandascloset.data.ImageWithTags
import com.example.mirandascloset.data.TagEntity
import com.example.mirandascloset.ui.theme.MirandasClosetTheme

class MainActivity : ComponentActivity() {
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

                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                val intent = Intent(this, AddPhotoActivity::class.java)
                                startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Photo")
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        TagDropdown(
                            tags = tagsWithImages.map { it.tag },
                            selectedTag = selectedTag,
                            onTagSelected = { selectedTag = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (imagesWithTags.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = androidx.compose.ui.Alignment.Center
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
                            ImageGrid(filteredImages, Modifier.weight(1f))
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
                .menuAnchor()
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
fun ImageGrid(images: List<ImageWithTags>, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(images.size) {
            images.forEach { image ->
                ImageListItem(image)
            }
        }
    }
}

