package fr.isen.nathangorga.tdandroid_socialnetwork

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import fr.isen.nathangorga.tdandroid_socialnetwork.login.LogActivity
import fr.isen.nathangorga.tdandroid_socialnetwork.profile.logout

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
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profilePictureBase64 by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isEditing by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Bouton de déconnexion en haut à droite
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { logout(context) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Déconnexion", color = Color.White, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Photo de profil + Nom + Bio
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color.Gray, CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.BottomEnd
            ) {
                Image(
                    painter = if (profilePictureBase64.isNotEmpty()) {
                        rememberImagePainter(decodeBase64ToBitmap(profilePictureBase64))
                    } else {
                        rememberImagePainter(R.drawable.default_profile_picture)
                    },
                    contentDescription = "Photo de profil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White, CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Modifier", tint = Color.Black)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Nom d'utilisateur et bio
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Nom d'utilisateur") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = username,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = bio,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bouton Modifier le profil
        Button(
            onClick = {
                if (isEditing) {
                    if (username.isNotBlank() && bio.isNotBlank()) {
                        saveProfile(userId, username, bio, imageUri, database, context) { base64 ->
                            profilePictureBase64 = base64
                            Toast.makeText(context, "Profil mis à jour", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                    }
                }
                isEditing = !isEditing
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text(if (isEditing) "Enregistrer" else "Modifier le profil", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Fonction de déconnexion.
 */
fun logout(context: Context, navController: NavHostController) {
    FirebaseAuth.getInstance().signOut()
    Toast.makeText(context, "Déconnecté", Toast.LENGTH_SHORT).show()

    // Rediriger vers la page d'accueil
    navController.navigate("journal") {
        popUpTo("profile/{userId}") { inclusive = true } // Supprime la page du profil de la pile de navigation
    }
}


/**
 * Fonction pour enregistrer le profil utilisateur avec Base64.
 */
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
fun decodeBase64ToBitmap(base64: String): android.graphics.Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
        android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}
