package fr.isen.nathangorga.tdandroid_socialnetwork

import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.database.*
import fr.isen.nathangorga.tdandroid_socialnetwork.models.Article

@Composable
fun FeedScreen(navController: NavHostController) {
    val articles = remember { mutableStateListOf<Article>() }
    val databaseRef = FirebaseDatabase.getInstance().getReference("articles")

    LaunchedEffect(Unit) {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                articles.clear()
                for (child in snapshot.children) {
                    val article = child.getValue(Article::class.java)
                    article?.let { articles.add(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error appropriately, e.g., log it or show a message to the user
                println("Firebase Error: ${error.message}")
            }
        })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB3E5FC)) // 🔵 Fond bleu clair (comme PublishScreen)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 📢 Titre du journal avec un cadre bleu
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2)) // 🔵 Bleu foncé
            ) {
                Text(
                    text = "📢 Mon Journal 📢",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // 📜 Liste des articles
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp) // ✅ Espacement entre les articles

            ) {
                items(articles.reversed()) { article ->
                    ArticleCard(article, databaseRef, navController)
                }
            }
        }
    }
}

@Composable
fun ArticleCard(article: Article, databaseRef: DatabaseReference, navController: NavHostController) {
    var likes by remember { mutableStateOf(article.likes ?: 0) }
    var isLiked by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("Utilisateur inconnu") }

    val userProfileRef = FirebaseDatabase.getInstance().getReference("users").child(article.userId ?: "")

    // 🔄 Récupérer le nom de l'utilisateur en temps réel
    LaunchedEffect(article.userId) {
        if (!article.userId.isNullOrEmpty()) {
            println("Tentative de récupération de l'utilisateur avec ID: ${article.userId}")
            userProfileRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userProfile = snapshot.getValue(UserProfile::class.java)
                    if (userProfile != null) {
                        println("Utilisateur trouvé: ${userProfile.username}")
                        username = userProfile.username
                    } else {
                        println("Aucun utilisateur trouvé pour cet ID.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Erreur de récupération du profil utilisateur: ${error.message}")
                }
            })
        } else {
            println("⚠️ UserID de l'article est null ou vide")
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f) // ✅ Centré et moins large
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // ✅ Affichage du nom d'utilisateur
            Text(
                text = username,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFB3E5FC), shape = RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = article.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 🖼️ Affichage de l'image si disponible
            article.imageUrl?.let { imageBase64 ->
                val decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Image du post",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray),
                    alignment = Alignment.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🕒 Informations et actions (date + like)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${article.date}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (!isLiked) {
                            likes += 1
                            isLiked = true
                            databaseRef.child(article.id).child("likes").setValue(likes)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else Color.Gray
                        )
                    }
                    Text(text = "$likes", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = {
                        navController.navigate("commentScreen/${article.id}")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = "Commentaires",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
