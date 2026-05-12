package br.com.grupokyly.apscoletor.data.remote

import br.com.grupokyly.apscoletor.data.remote.api.PickingApiService
import br.com.grupokyly.apscoletor.data.remote.dto.SyncBoxRequestDto
import br.com.grupokyly.apscoletor.data.remote.dto.SyncBoxResponseDto
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class RemoteDataSource @Inject constructor(
    private val api: PickingApiService
) {
    open suspend fun syncBox(request: SyncBoxRequestDto): Result<SyncBoxResponseDto> {
        return try {
            val response = api.syncBox(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Resposta do servidor vazia."))
                }
            } else {
                if (response.code() == 409) {
                    Result.failure(ConflictException("Caixa já sincronizada"))
                } else {
                    Result.failure(Exception("Erro na sincronização: ${response.code()}"))
                }
            }
        } catch (e: IOException) {
            Result.failure(NetworkException("Sem conexão de rede ou servidor inacessível."))
        } catch (e: HttpException) {
            Result.failure(Exception("Erro HTTP: ${e.code()} - ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Erro desconhecido: ${e.message}"))
        }
    }
    open suspend fun getBoxFull(papeletaCode: String): Result<SyncBoxRequestDto> {
        return try {
            val response = api.getBoxFull(papeletaCode)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Caixa vazia na resposta do servidor."))
                }
            } else {
                if (response.code() == 404) {
                    Result.failure(NotFoundException("Caixa não encontrada no servidor."))
                } else {
                    Result.failure(Exception("Erro ao baixar caixa: ${response.code()}"))
                }
            }
        } catch (e: IOException) {
            Result.failure(NetworkException("Sem conexão de rede ou servidor inacessível."))
        } catch (e: HttpException) {
            Result.failure(Exception("Erro HTTP: ${e.code()} - ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Erro desconhecido: ${e.message}"))
        }
    }
}

class NotFoundException(message: String) : Exception(message)
class NetworkException(message: String) : Exception(message)
class ConflictException(message: String) : Exception(message)
class ServerException(message: String) : Exception(message)
