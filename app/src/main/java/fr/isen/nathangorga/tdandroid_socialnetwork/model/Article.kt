package fr.isen.nathangorga.tdandroid_socialnetwork.models

import fr.isen.nathangorga.tdandroid_socialnetwork.model.Comment

data class Article(
    val id: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val date: String = "",
    val likes: Int = 0,
    var comments: Map<String, Comment>? = emptyMap(),
    val userId: String = "",
    val username: String = ""
)
