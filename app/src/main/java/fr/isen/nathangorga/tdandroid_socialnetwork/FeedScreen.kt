package fr.isen.nathangorga.tdandroid_socialnetwork

import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                // Gérer l'erreur
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)) // 🔹 Arrière-plan doux
            .padding(top = 16.dp)
    ) {
        Text(
            text = "📢 Mon Journal 📢",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, // Centre le texte
            modifier = Modifier
                .fillMaxWidth() // Étend le texte sur toute la largeur
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )


        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(articles.reversed()) { article -> // 🔹 Affichage du plus récent au plus ancien
                ArticleCard(article)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun ArticleCard(article: Article) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
            // 🟦 **Bulle bleue pour le texte**
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFB3E5FC), shape = RoundedCornerShape(12.dp)) // 🔹 Bleu ciel
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

            // 📸 **Affichage de l'image si disponible**
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

            // 🕒 **Date en gris, alignée à droite**
            Text(
                text = "Rédiger le  : ${article.date }",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}