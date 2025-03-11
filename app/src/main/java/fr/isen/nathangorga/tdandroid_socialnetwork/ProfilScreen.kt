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
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import fr.isen.nathangorga.tdandroid_socialnetwork.login.LogActivity

data class UserProfile(
    val userId: String = "",
    val username: String = "",
    val bio: String = "",
    val profilePictureBase64: String = ""
)

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ProfileScreen(navController: NavHostController) {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("users").child(userId)
        val context = LocalContext.current

        var username by remember { mutableStateOf("") }
        var bio by remember { mutableStateOf("") }
        var profilePictureBase64 by remember { mutableStateOf("") }
        var imageUri by remember { mutableStateOf<Uri?>(null) }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri -> if (uri != null) imageUri = uri }
        )

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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)) // Fond gris clair
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5)) // Fond gris clair
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd // Aligner tous les éléments en haut à droite
            ) {
                Button(

                    onClick = { logout(context) },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Déconnexion")
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Photo de profil
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

                // Carte contenant les infos utilisateur
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .animateContentSize()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Champ Nom d'utilisateur
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Nom d'utilisateur", color = Color.Black) },
                            textStyle = LocalTextStyle.current.copy(color = Color.Black),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Champ Bio
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio", color = Color.Black) },
                            textStyle = LocalTextStyle.current.copy(color = Color.Black),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bouton de sauvegarde
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2575FC))
                        ) {
                            Text("Enregistrer", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
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
// Fonction pour convertir une image en Base64
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
// Fonction pour décoder un Base64 en Bitmap

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

fun decodeBase64ToBitmap(base64: String): android.graphics.Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
        android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}
