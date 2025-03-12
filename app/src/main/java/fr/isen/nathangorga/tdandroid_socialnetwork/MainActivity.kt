package fr.isen.nathangorga.tdandroid_socialnetwork

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import fr.isen.nathangorga.tdandroid_socialnetwork.login.LogActivity
import fr.isen.nathangorga.tdandroid_socialnetwork.model.UserProfileDetailScreen
import fr.isen.nathangorga.tdandroid_socialnetwork.profile.SearchUserScreen
import fr.isen.nathangorga.tdandroid_socialnetwork.ui.theme.TDAndroidSocialNetworkTheme

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
                    finish()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun AppNavigation(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }

    val screens = listOf("journal", "publier", "recherche", "profile")
    val labels = listOf("Journal", "Publier", "Recherche", "Mon profil")
    val icons = listOf(
        Icons.AutoMirrored.Filled.Article,  // Icône article pour "Journal"
        Icons.Filled.Publish,  // Icône publication pour "Publier"
        Icons.Filled.Search,  // Icône recherche pour "Recherche"
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
                            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                            if (route == "profile" && currentUserId != null) {
                                navController.navigate("profile/$currentUserId")
                            } else {
                                navController.navigate(route)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController, startDestination = "journal") {
                composable("journal") { FeedScreen(navController) }
                composable("publier") { PublishScreen(navController) }
                composable("recherche") { SearchUserScreen(navController) }
                composable("userProfileDetail/{userId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    UserProfileDetailScreen(userId, navController)
                }


            composable("profile/{userId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    ProfileScreen(navController, userId)
                }
            }
        }
    }
}
