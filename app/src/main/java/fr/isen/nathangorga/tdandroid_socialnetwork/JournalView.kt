package fr.isen.nathangorga.tdandroid_socialnetwork

import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.database.*
import fr.isen.nathangorga.tdandroid_socialnetwork.models.Article

@Composable
fun JournalView(navController: NavHostController) {
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

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(articles) { article ->
            ArticleCard(article)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun ArticleCard(article: Article) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = article.text, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            article.imageUrl?.let { imageBase64 ->
                val decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Image du post",
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }
        }
    }
}
