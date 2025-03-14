package fr.isen.nathangorga.tdandroid_socialnetwork

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import fr.isen.nathangorga.tdandroid_socialnetwork.login.LogActivity
import fr.isen.nathangorga.tdandroid_socialnetwork.models.Article

data class UserProfile(
    val userId: String = "",
    val username: String = "",
    val bio: String = "",
    val profilePictureBase64: String = ""
)

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ProfileScreen(navController: NavHostController, userId: String) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val database = FirebaseDatabase.getInstance().getReference("users").child(userId)
    val articlesRef = FirebaseDatabase.getInstance().getReference("articles")

    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profilePictureBase64 by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var userArticles by remember { mutableStateOf<List<Article>>(emptyList()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> if (uri != null) imageUri = uri }
    )

    // Récupération des informations utilisateur
    LaunchedEffect(userId) {
        database.get().addOnSuccessListener { snapshot ->
            val userProfile = snapshot.getValue(UserProfile::class.java)
            if (userProfile != null) {
                username = userProfile.username
                bio = userProfile.bio
                profilePictureBase64 = userProfile.profilePictureBase64
            }
        }
    }

    // Récupération des articles postés par l'utilisateur
    LaunchedEffect(userId) {
        articlesRef.orderByChild("userId").equalTo(userId).get().addOnSuccessListener { snapshot ->
            val articlesList = mutableListOf<Article>()
            for (child in snapshot.children) {
                val article = child.getValue(Article::class.java)
                article?.let { articlesList.add(it) }
            }
            userArticles = articlesList
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB3E5FC)) // ✅ Fond bleu ciel
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // **Section Profil**
        Box(contentAlignment = Alignment.BottomEnd) {
            Image(
                painter = if (profilePictureBase64.isNotEmpty()) {
                    rememberImagePainter(decodeBase64ToBitmap(profilePictureBase64))
                } else {
                    rememberImagePainter(R.drawable.default_profile_picture)
                },
                contentDescription = "Photo de profil",
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color.Black, CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White, CircleShape)
                    .border(2.dp, Color.Black, CircleShape)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Modifier", tint = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // **Carte contenant les infos utilisateur**
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nom d'utilisateur") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Description") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // **Boutons Enregistrer & Se Déconnecter**
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            if (username.isNotBlank() && bio.isNotBlank()) {
                                saveProfile(userId, username, bio, imageUri, database, context) { base64 ->
                                    profilePictureBase64 = base64
                                    Toast.makeText(context, "Profil mis à jour", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2575FC)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Enregistrer", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { logout(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Déconnexion", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ **Bouton pour accéder aux messages**
        Button(
            onClick = { navController.navigate("inbox") }, // 🔥 Redirige vers l'Inbox
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text("📩 Voir mes messages", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // **Section Articles de l'utilisateur**
        Text(
            text = "Publications",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(vertical = 10.dp)
        )

        if (userArticles.isEmpty()) {
            Text(
                text = "Aucune publication pour l'instant.",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f) // ✅ Permet de scroller sans cacher le bouton
            ) {
                items(userArticles) { article ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = article.text,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (!article.imageUrl.isNullOrEmpty()) {
                                val bitmap = decodeBase64ToBitmap(article.imageUrl!!)
                                bitmap?.let {
                                    Image(
                                        painter = rememberAsyncImagePainter(it),
                                        contentDescription = "Image de l'article",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.Gray),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            Text(
                                text = "Publié le : ${article.date}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}


// Fonction pour décoder une image Base64
fun decodeBase64ToBitmap(base64: String): android.graphics.Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
        android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

// Fonction pour enregistrer le profil utilisateur avec Base64
@RequiresApi(Build.VERSION_CODES.P)
fun saveProfile(
    userId: String,
    username: String,
    bio: String,
    imageUri: Uri?,
    database: com.google.firebase.database.DatabaseReference,
    context: Context,
    onSuccess: (String) -> Unit
) {
    val profilePictureBase64 = if (imageUri != null) encodeImageToBase64(imageUri, context) ?: "" else ""

    val userProfile = UserProfile(userId, username, bio, profilePictureBase64)

    database.setValue(userProfile).addOnSuccessListener {
        onSuccess(profilePictureBase64)
    }
}
/*
Fonction pour convertir une image en Base64
@RequiresApi(Build.VERSION_CODES.P)
fun encodeImageToBase64(uri: Uri, context: Context): String? {
    return try {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
            android.graphics.ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        Base64.encodeToString(byteArray, Base64.DEFAULT)
    } catch (e: Exception) {
        null
    }
}
*/

fun logout(context: Context) {
    FirebaseAuth.getInstance().signOut()

    // Create an intent to navigate to LogActivity
    val intent = Intent(context, LogActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(intent)

    // Finish the current activity if context is an Activity
    if (context is Activity) {
        context.finish()
    }
}