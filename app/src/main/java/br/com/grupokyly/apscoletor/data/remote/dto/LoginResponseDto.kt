package br.com.grupokyly.apscoletor.data.remote.dto

data class LoginResponseDto(
    val token: String,
    val operatorName: String,
    val operatorCode: String,
    val supervisorName: String,
    val shift: String,
    val expiresAt: String
)
