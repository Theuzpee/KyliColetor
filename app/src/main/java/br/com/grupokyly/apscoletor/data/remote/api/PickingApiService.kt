package br.com.grupokyly.apscoletor.data.remote.api

import br.com.grupokyly.apscoletor.data.remote.dto.SyncBoxRequestDto
import br.com.grupokyly.apscoletor.data.remote.dto.SyncBoxResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PickingApiService {

    @POST("api/picking/sync-box")
    suspend fun syncBox(@Body request: SyncBoxRequestDto): Response<SyncBoxResponseDto>

    @GET("api/picking/health")
    suspend fun checkHealth(): Response<Map<String, String>>

    @GET("api/picking/boxes/{papeletaCode}")
    suspend fun checkBoxExists(@Path("papeletaCode") papeletaCode: String): Response<Map<String, Boolean>>

    @GET("api/picking/boxes/{papeletaCode}/full")
    suspend fun getBoxFull(@Path("papeletaCode") papeletaCode: String): Response<SyncBoxRequestDto>
}
