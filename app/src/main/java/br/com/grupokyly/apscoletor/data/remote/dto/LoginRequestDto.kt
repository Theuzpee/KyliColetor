package br.com.grupokyly.apscoletor.data.remote.dto

data class LoginRequestDto(
    val supervisorBarcode: String,
    val operatorBarcode: String
)
