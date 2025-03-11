package fr.isen.nathangorga.tdandroid_socialnetwork

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import fr.isen.nathangorga.tdandroid_socialnetwork.ProfilePage.ProfileEditScreen
import fr.isen.nathangorga.tdandroid_socialnetwork.ui.theme.TDAndroidSocialNetworkTheme
import fr.isen.nathangorga.tdandroid_socialnetwork.PublishScreen


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContent {
            TDAndroidSocialNetworkTheme {
                val navController = rememberNavController()
                val auth = remember { FirebaseAuth.getInstance() }
                val user = auth.currentUser

                if (user != null) {
                    AppNavigation(navController)
                } else {
                    val context = this
                    val intent = Intent(context, LogActivity::class.java)
                    context.startActivity(intent)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable

fun AppNavigation(navController: NavHostController) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val screens = listOf("journal", "publier", "plusTard", "profil")
    val labels = listOf("Journal", "Publier", "Plus tard", "Mon profil")
    val icons = listOf(
        Icons.AutoMirrored.Filled.Article,  // Icône article pour "Journal"
        Icons.Filled.Publish,  // Icône publication pour "Publier"
        Icons.AutoMirrored.Filled.Help,     // Icône aide pour "Plus tard"
        Icons.Filled.Person    // Icône profil pour "Mon profil"
    )


    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFB3E5FC), // Bleu plus clair
                modifier = Modifier.height(110.dp)
            ) {
                screens.forEachIndexed { index, route ->
                    NavigationBarItem(
                        icon = { Icon(imageVector = icons[index], contentDescription = labels[index], tint = Color.Black) },
                        label = { Text(labels[index], color = Color.Black) },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            navController.navigate(route)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController, startDestination = "journal") {
                composable("journal") { JournalView(navController) }
                composable("publier") { PublishScreen(navController) }
                composable("plusTard") { Page3Screen() }
                composable("profil") { ProfileEditScreen() }
            }
        }
    }
}

@Composable
fun Page1Screen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Page Journal")
    }
}

@Composable
fun Page2Screen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Page Publier")
    }
}

@Composable
fun Page3Screen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Page Plus tard")
    }
}

//@Composable
//fun Page4Screen() {
//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        Text("Page Profil")
//    }
//}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    TDAndroidSocialNetworkTheme {
        val navController = rememberNavController()
        AppNavigation(navController)
    }
}


