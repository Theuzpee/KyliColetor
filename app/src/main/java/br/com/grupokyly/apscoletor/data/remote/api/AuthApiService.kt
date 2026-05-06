package br.com.grupokyly.apscoletor.data.remote.api

import br.com.grupokyly.apscoletor.data.remote.dto.LoginRequestDto
import br.com.grupokyly.apscoletor.data.remote.dto.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto
}
