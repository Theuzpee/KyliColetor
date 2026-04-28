package br.com.grupokyly.apscoletor.test.fake

import br.com.grupokyly.apscoletor.data.remote.RemoteDataSource
import br.com.grupokyly.apscoletor.data.remote.api.PickingApiService
import br.com.grupokyly.apscoletor.data.remote.dto.SyncBoxRequestDto
import br.com.grupokyly.apscoletor.data.remote.dto.SyncBoxResponseDto

// Fake do RemoteDataSource para testes de Worker e afins, caso não queiramos mockar direto com MockK.
// Mas o prompt pediu para usar MockK direto no RemoteDataSource no Worker. 
// Vamos criar só para caso a gente use em algum lugar ou conforme a spec do user.
open class FakeRemoteDataSource(apiService: PickingApiService) : RemoteDataSource(apiService) {
    var syncResult: Result<SyncBoxResponseDto> = Result.success(fakeSyncResponse())
    
    override suspend fun syncBox(request: SyncBoxRequestDto): Result<SyncBoxResponseDto> {
        return syncResult
    }
}
