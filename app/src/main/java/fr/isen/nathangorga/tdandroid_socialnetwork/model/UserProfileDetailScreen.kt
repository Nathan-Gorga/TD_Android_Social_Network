package fr.isen.nathangorga.tdandroid_socialnetwork.model

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import fr.isen.nathangorga.tdandroid_socialnetwork.R
import fr.isen.nathangorga.tdandroid_socialnetwork.decodeBase64ToBitmap
import fr.isen.nathangorga.tdandroid_socialnetwork.models.Article
import com.google.firebase.database.*

data class SearchUser(
    val userId: String = "",
    val username: String = "",
    val bio: String = "",
    val profilePictureBase64: String = ""
)

@Composable
fun UserProfileDetailScreen(userId: String, navController: NavHostController) {
    val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
    val articlesRef = FirebaseDatabase.getInstance().getReference("articles")

    var user by remember { mutableStateOf<SearchUser?>(null) }
    var userArticles by remember { mutableStateOf<List<Article>>(emptyList()) }

    LaunchedEffect(userId) {
        databaseRef.get().addOnSuccessListener { snapshot ->
            user = snapshot.getValue(SearchUser::class.java)
        }

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
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            user?.let { profile ->
                // 🔙 Bouton Retour
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.Black)
                    }
                    Text(text = "Profil de ${profile.username}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

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

                Text(
                    text = profile.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = profile.bio,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ✅ Bouton "Envoyer un message" placé **au-dessus** des publications
                Button(
                    onClick = {
                        navController.navigate("chat/${userId}") // Redirige vers la messagerie avec l'ID de l'utilisateur
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("Envoyer un message", fontSize = 18.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp)) // ✅ Ajoute un espace avant les publications

                // 📜 Liste des articles postés
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
    }
}
