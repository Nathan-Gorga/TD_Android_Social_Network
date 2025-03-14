package fr.isen.nathangorga.tdandroid_socialnetwork.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import fr.isen.nathangorga.tdandroid_socialnetwork.R
import fr.isen.nathangorga.tdandroid_socialnetwork.login.LogActivity
import androidx.compose.ui.tooling.preview.Preview
import fr.isen.nathangorga.tdandroid_socialnetwork.ui.theme.TDAndroidSocialNetworkTheme

class ProfileEditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TDAndroidSocialNetworkTheme {
                val user = FirebaseAuth.getInstance().currentUser
                val userId = user?.uid
                if (userId != null) {
                    ProfileEditScreen(userId = userId)
                }
            }
        }
    }
}

@RequiresApi(28)
@Composable
fun ProfileEditScreen(userId: String) {
    val user = UserProfile.getFakeUser() // Fake user (à remplacer par Firebase)
    val context = LocalContext.current

    var username by remember { mutableStateOf(user.username) }
    var bio by remember { mutableStateOf("Décrivez-vous ici...") }
    var isEditing by remember { mutableStateOf(false) }

    // Image de profil
    val imageMap = mapOf(
        "default_profile_picture.png" to R.drawable.default_profile_picture,
        "pfp_jean.png" to R.drawable.pfp_jean
    )
    val profilePicture = imageMap[user.profilePictureName] ?: R.drawable.default_profile_picture

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Bouton de déconnexion en haut à droite
        Row(
            modifier = Modifier
                .fillMaxWidth(),
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

        Spacer(modifier = Modifier.height(10.dp))

        // Photo de profil + infos utilisateur à droite
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color.Gray, CircleShape)
                    .clickable { /* TODO: Ajouter modification de la photo */ },
                contentAlignment = Alignment.BottomEnd
            ) {
                Image(
                    painter = painterResource(id = profilePicture),
                    contentDescription = "Photo de profil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = { /* TODO: Modifier la photo de profil */ },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White, CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Modifier", tint = Color.Black)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
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
            onClick = { isEditing = !isEditing },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text(if (isEditing) "Enregistrer" else "Modifier le profil", fontSize = 18.sp, color = Color.White)
        }
    }
}

/**
 * Fonction de déconnexion et redirection vers l'écran de connexion.
 */
fun logout(context: Context) {
    FirebaseAuth.getInstance().signOut()
    val intent = Intent(context, LogActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(intent)
}

/**
 * Prévisualisation du design du profil.
 */
@Preview(showBackground = true)
@Composable
fun PreviewProfileEditScreen() {
    TDAndroidSocialNetworkTheme {
        ProfileEditScreen(userId = "preview_user_id")
    }
}
