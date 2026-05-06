package br.com.grupokyly.apscoletor.domain.model

data class UserSession(
    val token: String,
    val operatorName: String,
    val operatorCode: String,
    val supervisorName: String,
    val shift: String
)
