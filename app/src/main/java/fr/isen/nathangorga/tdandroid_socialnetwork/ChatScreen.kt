package fr.isen.nathangorga.tdandroid_socialnetwork.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(navController: NavHostController, receiverId: String) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val senderId = currentUser?.uid ?: return

    val messagesRef = FirebaseDatabase.getInstance().getReference("messages")
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var messageText by remember { mutableStateOf(TextFieldValue("")) }

    // Charger les messages en temps réel
    LaunchedEffect(receiverId) {
        messagesRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val messagesList = mutableListOf<Message>()
                for (child in snapshot.children) {
                    val message = child.getValue(Message::class.java)
                    if ((message?.senderId == senderId && message.receiverId == receiverId) ||
                        (message?.senderId == receiverId && message.receiverId == senderId)) {
                        message?.let { messagesList.add(it) }
                    }
                }
                messages = messagesList.sortedBy { it.timestamp } // ✅ Trie les messages du plus ancien au plus récent
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD)) // ✅ Fond bleu clair
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ✅ Messages affichés du plus ancien en haut au plus récent en bas
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = false // ✅ Affiche les messages dans l'ordre chronologique
        ) {
            items(messages) { message ->
                MessageItem(message, senderId)
            }
        }

        // ✅ Zone de saisie des messages en bas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text("Écrire un message...") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Blue
                )
            )
            IconButton(
                onClick = {
                    if (messageText.text.isNotBlank()) {
                        val newMessage = Message(
                            senderId = senderId,
                            receiverId = receiverId,
                            text = messageText.text,
                            timestamp = System.currentTimeMillis()
                        )
                        messagesRef.push().setValue(newMessage)
                        messageText = TextFieldValue("") // ✅ Efface le champ après envoi
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Envoyer", tint = Color.Blue)
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, currentUserId: String) {
    val isSentByMe = message.senderId == currentUserId
    val backgroundColor = if (isSentByMe) Color(0xFF64B5F6) else Color.White
    val textColor = if (isSentByMe) Color.White else Color.Black

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        contentAlignment = if (isSentByMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = message.text, fontSize = 16.sp, color = textColor)
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
