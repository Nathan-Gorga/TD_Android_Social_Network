package fr.isen.nathangorga.tdandroid_socialnetwork.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = 0
)

@Composable
fun InboxScreen(navController: NavHostController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: return

    val messagesRef = FirebaseDatabase.getInstance().getReference("messages")
    val usersRef = FirebaseDatabase.getInstance().getReference("users")

    var conversations by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var userNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Charger uniquement les messages **REÇUS**
    LaunchedEffect(userId) {
        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conversationMap = mutableMapOf<String, String>()
                val userIds = mutableSetOf<String>()

                for (child in snapshot.children) {
                    val message = child.getValue(Message::class.java)
                    if (message != null && message.receiverId == userId) { // ✅ Filtre : Seulement les messages reçus
                        val senderId = message.senderId
                        if (!conversationMap.containsKey(senderId)) {
                            conversationMap[senderId] = message.text
                        }
                        userIds.add(senderId)
                    }
                }

                conversations = conversationMap

                // Récupérer les noms des utilisateurs qui ont envoyé un message
                userIds.forEach { uid ->
                    usersRef.child(uid).get().addOnSuccessListener { userSnapshot ->
                        val username = userSnapshot.child("username").value as? String ?: "Utilisateur inconnu"
                        userNames = userNames + (uid to username) // Mise à jour du cache des noms
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD)) // ✅ Fond bleu clair
            .padding(16.dp)
    ) {
        Text(
            text = "📩 Messages reçus",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )

        LazyColumn {
            items(conversations.entries.toList()) { (senderId, lastMessage) -> // ✅ Affiche uniquement les messages reçus
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { navController.navigate("chat/$senderId") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = userNames[senderId] ?: "Chargement...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = lastMessage, fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}
