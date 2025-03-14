package fr.isen.nathangorga.tdandroid_socialnetwork

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import fr.isen.nathangorga.tdandroid_socialnetwork.models.Article
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign


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
            .background(Color(0xFFB3E5FC)) // 🔵 Fond bleu clair (même couleur que la navbar)
            .padding(20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 📢 Bloc bleu pour "Créer une publication"
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f) // Même largeur que le titre
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2)) // 🔵 Bleu foncé
            ) {
                Text(

                    text = "✏\uFE0F  Ecrire un truc cool ✏\uFE0F",

                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Zone de texte
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Exprimez-vous...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 🖼️ Aperçu de l'image sélectionnée
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = RoundedCornerShape(16.dp) // 🔵 Coins arrondis
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF42A5F5), Color(0xFF1976D2))
                                    )
                                )
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Ajouter une image", tint = Color.White, modifier = Modifier.size(40.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 📩 Bouton de publication stylisé
            Button(
                onClick = {
                    isLoading = true
                    if (imageUri != null) {
                        postArticle(text, imageUri!!, navController, context)
                    } else {
                        postArticle(text, null, navController, context)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .clip(RoundedCornerShape(12.dp)), // 🔵 Bouton arrondi
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)) // 🔵 Bleu vif
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
    val currentDate = Date()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val formattedDate = dateFormat.format(currentDate)

    // ✅ Récupérer l'ID de l'utilisateur connecté
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // ✅ Vérification si l'utilisateur est bien connecté
    if (currentUserId.isEmpty()) {
        println("⚠️ Erreur : Aucun utilisateur connecté")
        return
    }

    val article = Article(
        id = databaseRef.push().key ?: "",
        text = text,
        imageUrl = imageBase64,
        date = formattedDate,
        userId = currentUserId // 🔥 Enregistrer l'ID de l'utilisateur 🔥
    )

    databaseRef.child(article.id).setValue(article).addOnCompleteListener {
        navController.navigate("journal") // 🔄 Redirection après publication
        println("✅ Article publié avec succès : ${article.id}")
        navController.navigate("journal") // Redirection après publication
    }.addOnFailureListener {
        println("❌ Erreur lors de la publication : ${it.message}")
    }
}
