package com.example.mirandascloset

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mirandascloset.data.AppDatabase
import com.example.mirandascloset.data.ImageDao
import com.example.mirandascloset.data.ImageEntity
import com.example.mirandascloset.data.ImageTagCrossRef
import com.example.mirandascloset.data.TagEntity
import com.example.mirandascloset.ui.theme.MirandasClosetTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

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
    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var tags by remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as Activity

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                photoBitmap = imageBitmap
            }
        }
    }

    fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    fun requestCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        } else {
            openCamera()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Photo") },
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
                .padding(24.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoBitmap != null) {
                        Image(
                            bitmap = photoBitmap!!.asImageBitmap(),
                            contentDescription = "Captured photo",
                            modifier = Modifier.size(220.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Camera icon",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { requestCameraPermissionAndOpenCamera() },
                    modifier = Modifier
                        .wrapContentWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("Take Picture")
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Add tags (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (photoBitmap == null) {
                            Toast.makeText(context, "Take a picture first!", Toast.LENGTH_SHORT).show()
                        } else {
                            val db = AppDatabase.getInstance(context)
                            val imageDao = db.imageDao()

                            CoroutineScope(Dispatchers.IO).launch {
                                saveImageWithTags(
                                    imageDao = imageDao,
                                    bitmap = photoBitmap!!,
                                    context = context,
                                    tagNames = tags.split(",")
                                )
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                                    onBack()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Photo with Tags")
                }
            }
        }
    }
}

suspend fun saveImageWithTags(
    imageDao: ImageDao,
    bitmap: Bitmap,
    context: Context,
    tagNames: List<String>
) {
    // 1. Save bitmap to internal storage
    val filename = "IMG_${UUID.randomUUID()}.jpg"
    val file = File(context.filesDir, filename)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
    }

    // 2. Insert image row, get imageId
    val imageId = imageDao.insertImage(ImageEntity(filePath = file.absolutePath))

    // 3. For each tag, insert if needed and cross-ref
    for (tag in tagNames.map { it.trim().lowercase() }.filter { it.isNotEmpty() }) {
        var tagId = imageDao.getTagByName(tag)?.tagId
        if (tagId == null) {
            tagId = imageDao.insertTag(TagEntity(name = tag))
        }
        imageDao.insertImageTagCrossRef(ImageTagCrossRef(imageId, tagId!!))
    }
}