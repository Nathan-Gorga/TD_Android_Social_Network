package fr.isen.nathangorga.tdandroid_socialnetwork

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import fr.isen.nathangorga.tdandroid_socialnetwork.ui.theme.TDAndroidSocialNetworkTheme
//import fr.isen.nathangorga.tdandroid_socialnetwork.ui.theme.LoginScreen

class MainActivity : ComponentActivity() {
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
@Composable
fun AppNavigation(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }

    val screens = listOf("Page1", "Page2", "Page3", "Page4")
    val icons = listOf(
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground
    )

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(8.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar {
                screens.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(painterResource(id = icons[index]), contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            navController.navigate(item)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController, startDestination = "Page1") {
                composable("Page1") { Page1Screen() }
                composable("Page2") { Page2Screen() }
                composable("Page3") { Page3Screen() }
                composable("Page4") { Page4Screen() }
            }
        }
    }
}

@Composable
fun Page1Screen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Page 1")
    }
}

@Composable
fun Page2Screen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Page 2")
    }
}

@Composable
fun Page3Screen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Page 3")
    }
}

@Composable
fun Page4Screen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Page 4")
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    TDAndroidSocialNetworkTheme {
        val navController = rememberNavController()
        AppNavigation(navController)
    }
}