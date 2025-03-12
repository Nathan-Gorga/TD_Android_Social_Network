package fr.isen.nathangorga.tdandroid_socialnetwork.profile

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.database.*
import fr.isen.nathangorga.tdandroid_socialnetwork.R
import fr.isen.nathangorga.tdandroid_socialnetwork.decodeBase64ToBitmap
import fr.isen.nathangorga.tdandroid_socialnetwork.models.Article

data class SearchUser(
    val userId: String = "",
    val username: String = "",
    val bio: String = "",
    val profilePictureBase64: String = ""
)

@Composable
fun SearchUserScreen(navController: NavHostController) {
    val databaseRef = FirebaseDatabase.getInstance().getReference("users")
    var searchQuery by remember { mutableStateOf("") }
    var users by remember { mutableStateOf<List<SearchUser>>(emptyList()) }

    // 🔄 Mise à jour de la liste en fonction de la recherche
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            databaseRef.orderByChild("username")
                .startAt(searchQuery)
                .endAt("$searchQuery\uf8ff")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userList = mutableListOf<SearchUser>()
                        for (child in snapshot.children) {
                            val user = child.getValue(SearchUser::class.java)
                            user?.let { userList.add(it) }
                        }
                        users = userList
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        } else {
            users = emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        // 📌 Titre
        Text(
            text = "🔍 Recherche Un Papi",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2575FC),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        // 🔍 Champ de recherche
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Rechercher un autre papi...") },
            leadingIcon = { Icon(Icons.Default.PersonSearch, contentDescription = "Recherche") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2575FC),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFF2575FC),
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 📜 Liste des utilisateurs trouvés
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(users) { user ->
                UserSearchItem(user, navController)
            }
        }
    }
}

// 🎨 Carte pour chaque utilisateur trouvé
@Composable
fun UserSearchItem(user: SearchUser, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { navController.navigate("userProfileDetail/${user.userId}") } // ✅ Ouvre une nouvelle page
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = if (user.profilePictureBase64.isNotEmpty()) {
                    rememberAsyncImagePainter(decodeBase64ToBitmap(user.profilePictureBase64))
                } else {
                    rememberAsyncImagePainter(R.drawable.default_profile_picture)
                },
                contentDescription = "Photo de profil",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(text = user.username, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = "Voir le profil", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}


// 📌 **Profil utilisateur + Ses articles**
@Composable
fun UserProfileScreen(userId: String, navController: NavHostController) {
    val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
    val articlesRef = FirebaseDatabase.getInstance().getReference("articles")

    var user by remember { mutableStateOf<SearchUser?>(null) }
    var userArticles by remember { mutableStateOf<List<Article>>(emptyList()) }

    LaunchedEffect(userId) {
        // Récupérer infos utilisateur
        databaseRef.get().addOnSuccessListener { snapshot ->
            user = snapshot.getValue(SearchUser::class.java)
        }

        // Récupérer ses articles
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
            .background(Color.White)
            .padding(16.dp)
    ) {
        user?.let { profile ->
            // 📸 Photo + Nom + Bio
            Image(
                painter = if (profile.profilePictureBase64.isNotEmpty()) {
                    rememberAsyncImagePainter(decodeBase64ToBitmap(profile.profilePictureBase64))
                } else {
                    rememberAsyncImagePainter(R.drawable.default_profile_picture)
                },
                contentDescription = "Photo de profil",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = profile.username, fontWeight = FontWeight.Bold, fontSize = 22.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(text = profile.bio, fontSize = 16.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))

            Spacer(modifier = Modifier.height(20.dp))

            // 📜 Liste des articles postés
            LazyColumn {
                items(userArticles) { article ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = article.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text(text = "Publié le : ${article.date}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
