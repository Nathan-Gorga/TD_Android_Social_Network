package fr.isen.nathangorga.tdandroid_socialnetwork

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.database.FirebaseDatabase
import fr.isen.nathangorga.tdandroid_socialnetwork.models.Article
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import com.google.type.Date
import java.util.Locale
import kotlin.text.format

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(navController: NavHostController) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0)) // Fond gris clair
            .padding(20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Publier un message",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Zone de texte
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Exprimez-vous...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                //colors = TextFieldDefaults.outlinedTextFieldColors(),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Image sélectionnée + bouton pour ajouter une photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Image sélectionnée",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF64B5F6))
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = "Ajouter une image", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bouton publier
            Button(
                onClick = {
                    isLoading = true
                    if (imageUri != null) {
                        postArticle(text, imageUri!!, navController, context) // Avec image
                    } else {
                        postArticle(text, null, navController, context) // Juste du texte
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text("Publier", fontSize = 20.sp, color = Color.White)
                }
            }
        }
    }
}

/**
 * Convertit une image en Base64
 */
@RequiresApi(Build.VERSION_CODES.P)
fun encodeImageToBase64(uri: Uri, context: Context): String? {
    return try {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        Base64.encodeToString(byteArray, Base64.DEFAULT)
    } catch (e: Exception) {
        null
    }
}

/**
 * Publie un article dans Firebase avec ou sans image
 */
@RequiresApi(Build.VERSION_CODES.P)
fun postArticle(text: String, imageUri: Uri?, navController: NavHostController, context: Context) {
    val databaseRef = FirebaseDatabase.getInstance().getReference("articles")

    val imageBase64 = imageUri?.let { encodeImageToBase64(it, context) } // Convertir l'image si elle existe
    val currentDate = java.util.Date()
    val dateFormat = android . icu . text . SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val formattedDate = dateFormat.format(currentDate)
    val article = Article(
        id = databaseRef.push().key ?: "",
        text = text,
        imageUrl = imageBase64, // Stockage en Base64
        date = formattedDate
    )

    databaseRef.child(article.id).setValue(article).addOnCompleteListener {
        navController.navigate("journal") // Redirection après publication
    }
}
