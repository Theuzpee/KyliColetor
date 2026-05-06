package br.com.grupokyly.apscoletor.data.remote

import br.com.grupokyly.apscoletor.data.remote.api.AuthApiService
import br.com.grupokyly.apscoletor.data.remote.dto.LoginRequestDto
import br.com.grupokyly.apscoletor.data.remote.dto.LoginResponseDto
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val api: AuthApiService
) {
    suspend fun login(
        supervisorBarcode: String,
        operatorBarcode: String
    ): Result<LoginResponseDto> {
        return try {
            val response = api.login(LoginRequestDto(supervisorBarcode, operatorBarcode))
            Result.success(response)
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> Result.failure(Exception("Crachá não reconhecido."))
                429 -> Result.failure(Exception("Muitas tentativas. Aguarde 1 minuto."))
                else -> Result.failure(Exception("Erro no servidor. Tente novamente."))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Sem conexão. Verifique a rede."))
        } catch (e: Exception) {
            Result.failure(Exception("Erro desconhecido. Tente novamente."))
        }
    }
}
