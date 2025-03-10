package fr.isen.nathangorga.tdandroid_socialnetwork.ProfilePage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.isen.nathangorga.tdandroid_socialnetwork.R
import fr.isen.nathangorga.tdandroid_socialnetwork.ui.theme.TDAndroidSocialNetworkTheme

class ProfileEditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TDAndroidSocialNetworkTheme {
                ProfileEditScreen()
            }
        }
    }
}

@Composable
fun ProfileEditScreen() {
    val user = UserProfile.getFakeUser()

    val pfp = remember { mutableIntStateOf(R.drawable.ic_launcher_foreground) }
    val imageMap = mapOf(
        "default_profile_picture.png" to R.drawable.default_profile_picture,
        "pfp_jean.png" to R.drawable.pfp_jean
    )
    pfp.intValue = imageMap[user.profilePictureName] ?: R.drawable.ic_launcher_foreground

    var username by remember { mutableStateOf(TextFieldValue(user.username)) }
    var firstName by remember { mutableStateOf(TextFieldValue(user.firstName)) }
    var lastName by remember { mutableStateOf(TextFieldValue(user.lastName)) }
    val profilePicture by remember { mutableIntStateOf(pfp.intValue) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        CustomTextField("Prénom", initialValue = user.firstName)
        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField("Nom", initialValue = user.lastName)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { /* Sauvegarder les modifications */ }) {
            Text("Enregistrer")
        }
    }
}

@Composable
fun CustomTextField(label: String, initialValue: String) {
    var text by remember { mutableStateOf(initialValue) }
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(label) },
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



@Preview(showBackground = true)
@Composable
fun PreviewProfileEditScreen() {
    TDAndroidSocialNetworkTheme {
        ProfileEditScreen()
    }
}