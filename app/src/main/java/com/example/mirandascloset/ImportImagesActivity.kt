package com.example.mirandascloset

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mirandascloset.data.AppDatabase
import com.example.mirandascloset.ui.theme.MirandasClosetTheme
import com.example.mirandascloset.ui.views.EditImageView

class ImportImagesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MirandasClosetTheme {
                ImportImagesScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ImportImagesScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val imageDao = remember { db.imageDao() }
    var importedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var current by remember { mutableIntStateOf(0) }
    val tracker = if (importedUris.count() > 0) "${current + 1}/${importedUris.count()}" else ""

    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        importedUris = uris
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Images $tracker") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxWidth().padding(innerPadding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (importedUris.isEmpty()) {
                Spacer(
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        pickImagesLauncher.launch(arrayOf("image/*"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Images", modifier = Modifier.padding(8.dp))
                }
            } else {
                var bitmap by remember { mutableStateOf<Bitmap?>(null) }

                context.contentResolver.openInputStream(importedUris[current])?.use { input ->
                    bitmap = BitmapFactory.decodeStream(input)
                }

                EditImageView(null, bitmap, null, context, imageDao, onBack = {
                    if (importedUris.indices.contains(current + 1)) {
                        current += 1
                    } else {
                        onBack()
                    }
                })
            }
        }
    }
}