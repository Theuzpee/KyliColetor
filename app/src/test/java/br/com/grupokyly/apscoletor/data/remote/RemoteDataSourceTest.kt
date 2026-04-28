package br.com.grupokyly.apscoletor.data.remote

import br.com.grupokyly.apscoletor.data.remote.api.PickingApiService
import br.com.grupokyly.apscoletor.data.remote.dto.fakeSyncBoxRequestDto
import br.com.grupokyly.apscoletor.test.fake.fakeSyncResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class RemoteDataSourceTest {

    private lateinit var apiService: PickingApiService
    private lateinit var remoteDataSource: RemoteDataSource

    @Before
    fun setup() {
        apiService = mockk(relaxed = true)
        remoteDataSource = RemoteDataSource(apiService)
    }

    @Test
    fun `syncBox returns failure with portuguese message on IOException`() = runTest {
        coEvery { apiService.syncBox(any()) } throws IOException("Timeout")

        val result = remoteDataSource.syncBox(fakeSyncBoxRequestDto())

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Sem conexão de rede", ignoreCase = true))
    }

    @Test
    fun `syncBox returns failure with portuguese message on HttpException 500`() = runTest {
        coEvery { apiService.syncBox(any()) } throws HttpException(Response.error<Any>(500, "".toResponseBody()))

        val result = remoteDataSource.syncBox(fakeSyncBoxRequestDto())

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Erro HTTP: 500", ignoreCase = true))
    }

    @Test
    fun `syncBox returns success with correct DTO`() = runTest {
        val expected = fakeSyncResponse()
        coEvery { apiService.syncBox(any()) } returns Response.success(expected)

        val result = remoteDataSource.syncBox(fakeSyncBoxRequestDto())

        assertTrue(result.isSuccess)
        assertEquals(expected.syncId, result.getOrNull()!!.syncId)
    }
}
