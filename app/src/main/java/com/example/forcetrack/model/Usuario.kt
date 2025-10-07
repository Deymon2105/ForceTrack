package com.example.forcetrack.model

open class Usuario(
    val username: String,
    val email: String,
    var password: String
)

class UsuarioPremium(username: String, email: String, password: String, val perks: List<String>)
    : Usuario(username, email, password)
