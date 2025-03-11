package fr.isen.nathangorga.tdandroid_socialnetwork.model

data class Comment(
    var id: String? = null,
    var author: String? = null,
    var content: String? = null,
    var date: String? = ""
)
{
    constructor() : this(null, null, null, null)}