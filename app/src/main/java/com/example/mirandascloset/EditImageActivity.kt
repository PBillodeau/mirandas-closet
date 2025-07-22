package com.example.mirandascloset

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.mirandascloset.data.*
import com.example.mirandascloset.ui.theme.MirandasClosetTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class EditImageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageId = intent.getLongExtra("imageId", -1L)
        if (imageId == -1L) {
            Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setContent {
            MirandasClosetTheme {
                EditImageScreen(
                    imageId = imageId,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditImageScreen(imageId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val imageDao = remember { db.imageDao() }
    var imageWithTags by remember { mutableStateOf<ImageWithTags?>(null) }
    var tags by remember { mutableStateOf("") }
    val openAlertDialog = remember { mutableStateOf(false) }

    LaunchedEffect(imageId) {
        launch(Dispatchers.IO) {
            imageWithTags = imageDao.getImageById(imageId)
            tags = imageWithTags?.tags?.joinToString(", ") { it.name } ?: ""
        }
    }
    when { openAlertDialog.value -> {
        BasicAlertDialog(
            onDismissRequest = { openAlertDialog.value = false },
            properties = DialogProperties(), content = {
                Surface(
                    modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp)) {
                        Text("Are you sure you want to delete this photo?")
                        Row(modifier = Modifier.padding(16.dp).align(Alignment.End)) {
                            TextButton (
                                onClick = {
                                    openAlertDialog.value = false
                                }
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        imageWithTags?.let { iwt ->
                                            imageDao.deleteImage(imageWithTags!!)
                                            launch(Dispatchers.Main) {
                                                Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show()
                                                onBack()
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Confirm")
                            }
                        }
                    }
                }
            })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Image") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            openAlertDialog.value = true
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Image")
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            if (imageWithTags != null) {
                val imgFile = File(imageWithTags!!.image.filePath)
                val bitmap = if (imgFile.exists()) BitmapFactory.decodeFile(imgFile.absolutePath) else null

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.weight(1f).background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Saved photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma-separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                imageWithTags?.let { iwt ->
                                    imageDao.updateImageTags(iwt.image.imageId, tags)
                                    launch(Dispatchers.Main) {
                                        Toast.makeText(context, "Tags updated", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save", modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}
