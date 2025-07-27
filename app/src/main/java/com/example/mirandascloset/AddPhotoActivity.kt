package com.example.mirandascloset

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mirandascloset.data.AppDatabase
import com.example.mirandascloset.ui.theme.MirandasClosetTheme
import com.example.mirandascloset.ui.views.EditImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddPhotoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MirandasClosetTheme {
                AddPhotoScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPhotoScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val imageDao = remember { db.imageDao() }
    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var tags by remember { mutableStateOf("") }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            photoBitmap = bitmap
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Photo") },
                actions = {
                    if (photoBitmap !== null) {
                        IconButton(
                            onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    imageDao.createImage(context, photoBitmap!!, tags)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp)
        ) {
            EditImageView(photoBitmap, cameraLauncher, context, tags) { value -> tags = value }
        }
    }
}