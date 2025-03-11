package fr.isen.nathangorga.tdandroid_socialnetwork.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import fr.isen.nathangorga.tdandroid_socialnetwork.R
import fr.isen.nathangorga.tdandroid_socialnetwork.login.LogActivity
import fr.isen.nathangorga.tdandroid_socialnetwork.ui.theme.DarkBlue
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

@Composable
fun ProfileEditScreen(userId: String) { // Pass context as a parameter
    //TODO: remplacer fake user par un user de firebase
    //À SUPPRIMER CE BLOC
    val user = UserProfile.getFakeUser()

    val pfp = remember { mutableIntStateOf(R.drawable.ic_launcher_foreground) }
    val imageMap = mapOf(
        "default_profile_picture.png" to R.drawable.default_profile_picture,
        "pfp_jean.png" to R.drawable.pfp_jean
    )
    pfp.intValue = imageMap[user.profilePictureName] ?: R.drawable.ic_launcher_foreground

    val profilePicture by remember { mutableIntStateOf(pfp.intValue) }
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profil de l'utilisateur : $userId")
        // Bouton de déconnexion en haut à droite
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { logout(context = context) }, // Pass context to fr.isen.nathangorga.tdandroid_socialnetwork.profile.Logout
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Déconnexion")
            }
        }

        Image(
            painter = painterResource(id = profilePicture),
            contentDescription = "Photo de profil",
            modifier = Modifier
                .size(100.dp)
                .clickable { /* Ajouter logique de changement image */ },
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField("Nom d'utilisateur", initialValue = user.username)
        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField("Courriel", initialValue = user.email)
        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField("Prénom", initialValue = user.firstName)
        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField("Nom", initialValue = user.lastName)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { /* Sauvegarder les modifications */ }) {
            Text("Enregistrer")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(label: String, initialValue: String) {
    var text by remember { mutableStateOf(initialValue) }
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(label) },
        colors = TextFieldDefaults.colors( // Remplace outlinedTextFieldColors() par colors()
            focusedIndicatorColor = DarkBlue, // Bordure quand focus
            unfocusedIndicatorColor = Color.Gray, // Bordure quand pas focus
            focusedLabelColor = DarkBlue, // Couleur du label quand focus
            unfocusedLabelColor = Color.Gray // Couleur du label sans focus
        ),
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    if (!isFocused && (initialValue == text)) {
                        text = ""
                    }
                    isFocused = true
                } else {
                    if (text.isBlank()) {
                        text = initialValue
                    }
                    isFocused = false
                }
            }
    )
}

fun logout(context: Context) {
    FirebaseAuth.getInstance().signOut()

    // Rediriger vers l'écran de connexion
    val intent = Intent(context, LogActivity::class.java)
    intent.flags =
        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Empêche de revenir en arrière avec le bouton retour
    context.startActivity(intent)
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileEditScreen() {
    TDAndroidSocialNetworkTheme {
        ProfileEditScreen(userId = "preview_user_id")
    }
}
