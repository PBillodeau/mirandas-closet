package com.example.mirandascloset

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mirandascloset.data.AppDatabase
import com.example.mirandascloset.ui.theme.MirandasClosetTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()

    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        importedUris = uris
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Images") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (importedUris.isEmpty()) {
                    Spacer(modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.background))
                    Button(
                        onClick = {
                            pickImagesLauncher.launch(arrayOf("image/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Select Images", modifier = Modifier.padding(8.dp))
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        modifier = Modifier.fillMaxHeight(0.90f).fillMaxWidth(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(importedUris.size) { uri ->
                            ImportImageListItem(
                                uri = importedUris[uri]
                            )
                        }
                    }
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                importedUris.forEach { uri ->
                                    val inputStream = context.contentResolver.openInputStream(uri)
                                    val photoBitmap = BitmapFactory.decodeStream(inputStream)
                                    imageDao.createImage(context, photoBitmap!!, "")
                                }
                                launch(Dispatchers.Main) {
                                    Toast.makeText(context, "Images imported!", Toast.LENGTH_SHORT).show()
                                    onBack()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Import Images", modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ImportImageListItem(
    uri: Uri
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            bitmap = BitmapFactory.decodeStream(input)
        }
    } catch (_: Exception) {
        bitmap = null
    }

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
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Saved photo",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
