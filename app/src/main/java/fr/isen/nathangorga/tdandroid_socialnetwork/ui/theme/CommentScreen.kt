package fr.isen.nathangorga.tdandroid_socialnetwork

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import fr.isen.nathangorga.tdandroid_socialnetwork.model.Comment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentScreen(articleId: String, navController: NavHostController) {
    val databaseRef = FirebaseDatabase.getInstance().getReference("articles/$articleId/comments")
    val comments = remember { mutableStateListOf<Comment>() }
    var commentText by remember { mutableStateOf("") }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // 🔄 Récupération des commentaires en temps réel depuis Firebase
    LaunchedEffect(Unit) {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                comments.clear()
                for (child in snapshot.children) {
                    val comment = child.getValue(Comment::class.java)
                    comment?.let { comments.add(it) }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFF8F9FA)), // 🔹 Fond doux
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🔙 Titre avec bouton retour
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
            }
            Text(
                text = "💬 Commentaires",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 📜 Affichage des commentaires
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp)) // 🔹 Arrondi pour style
                .background(Color.White)
                .padding(8.dp)
        ) {
            items(comments) { comment ->
                CommentItem(comment)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 📝 Champ pour écrire un commentaire
        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            label = { Text("Ajouter un commentaire...") },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)), // 🔹 Arrondi
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFB3E5FC), // 🔵 Bleu clair
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFFB3E5FC), // 🔵 Curseur
                focusedLabelColor = Color(0xFFB3E5FC),
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 📩 Bouton pour envoyer le commentaire
        Button(
            onClick = {
                if (commentText.isNotBlank()) {
                    val newComment = Comment(
                        id = databaseRef.push().key ?: "",
                        content = commentText,
                        userId = currentUserId, // ✅ Récupérer l'ID de l'utilisateur connecté
                        date = System.currentTimeMillis().toString()
                    )
                    newComment.id?.let { databaseRef.child(it).setValue(newComment) }
                    commentText = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)) // 🔹 Style arrondi
        ) {
            Text("Envoyer", fontWeight = FontWeight.Bold)
        }
    }
}

// 🔹 Élément d'affichage pour chaque commentaire (design cohérent avec `FeedScreen`)
@Composable
fun CommentItem(comment: Comment) {
    var username by remember { mutableStateOf("Utilisateur inconnu") }
    val userProfileRef = FirebaseDatabase.getInstance().getReference("users").child(comment.userId ?: "")

    LaunchedEffect(comment.userId) {
        if (!comment.userId.isNullOrEmpty()) {
            userProfileRef.get().addOnSuccessListener { snapshot ->
                val userProfile = snapshot.getValue(UserProfile::class.java)
                username = userProfile?.username ?: "Utilisateur inconnu"
            }.addOnFailureListener {
                println("Erreur lors de la récupération du username : ${it.message}")
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .background(Color(0xFFB3E5FC), shape = RoundedCornerShape(12.dp)) // 🔵 Fond bleu doux
                .padding(12.dp)
        ) {
            // 🔹 Affichage du username
            Text(
                text = username,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            comment.content?.let {
                Text(
                    text = it,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Posté le ${comment.date}",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
