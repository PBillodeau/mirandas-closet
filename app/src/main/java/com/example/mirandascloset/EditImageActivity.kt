package com.example.mirandascloset

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.mirandascloset.data.*
import com.example.mirandascloset.ui.theme.MirandasClosetTheme
import com.example.mirandascloset.ui.views.EditImageView
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
    val openAlertDialog = remember { mutableStateOf(false) }

    LaunchedEffect(imageId) {
        launch(Dispatchers.IO) {
            imageWithTags = imageDao.getImageById(imageId)
        }
    }

    when { openAlertDialog.value -> {
            DeleteConfirmation(
                dialog = openAlertDialog,
                onBack = { onBack() },
                context = context,
                imageDao = imageDao,
                imageWithTags = imageWithTags!!
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Image") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {openAlertDialog.value = true}) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Image")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp)
        ) {
            if (imageWithTags != null) {
                val imgFile = File(imageWithTags!!.image.filePath)
                val bitmap = if (imgFile.exists()) BitmapFactory.decodeFile(imgFile.absolutePath) else null
                EditImageView(imageWithTags, bitmap, null, context, imageDao, onBack={onBack()})
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteConfirmation(
    context: Context,
    imageWithTags: ImageWithTags,
    imageDao: ImageDao,
    dialog: MutableState<Boolean>,
    onBack: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = { dialog.value = false },
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
                                dialog.value = false
                            }
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    imageWithTags.let { iwt ->
                                        imageDao.deleteImage(imageWithTags)
                                        launch(Dispatchers.Main) {
                                            Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show()
                                            onBack()
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    )
}
