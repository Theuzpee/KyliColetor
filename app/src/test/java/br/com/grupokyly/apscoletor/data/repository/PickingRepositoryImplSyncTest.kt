package br.com.grupokyly.apscoletor.data.repository

import br.com.grupokyly.apscoletor.data.local.dao.BoxDao
import br.com.grupokyly.apscoletor.data.local.dao.PickingItemDao
import br.com.grupokyly.apscoletor.data.local.dao.ScannedPieceDao
import br.com.grupokyly.apscoletor.data.local.dao.DivergenceDao
import br.com.grupokyly.apscoletor.data.remote.RemoteDataSource
import br.com.grupokyly.apscoletor.data.sync.SyncScheduler
import br.com.grupokyly.apscoletor.test.fake.fakeBoxEntity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PickingRepositoryImplSyncTest {

    private lateinit var boxDao: BoxDao
    private lateinit var pickingItemDao: PickingItemDao
    private lateinit var scannedPieceDao: ScannedPieceDao
    private lateinit var divergenceDao: DivergenceDao
    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var syncScheduler: SyncScheduler
    private lateinit var repository: PickingRepositoryImpl

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        boxDao = mockk(relaxed = true)
        pickingItemDao = mockk(relaxed = true)
        scannedPieceDao = mockk(relaxed = true)
        divergenceDao = mockk(relaxed = true)
        remoteDataSource = mockk(relaxed = true)
        syncScheduler = mockk(relaxed = true)

        repository = PickingRepositoryImpl(
            boxDao = boxDao,
            pickingItemDao = pickingItemDao,
            scannedPieceDao = scannedPieceDao,
            divergenceDao = divergenceDao,
            dispatcher = testDispatcher,
            syncScheduler = syncScheduler,
            remoteDataSource = remoteDataSource
        )
    }

    @Test
    fun `finalizeBox schedules sync on success`() = runTest {
        val fakeBoxEntity = fakeBoxEntity()
        coEvery { boxDao.getBoxById(1L) } returns fakeBoxEntity
        every { pickingItemDao.getItemsByBox(1L) } returns flowOf(emptyList())

        repository.finalizeBox(boxId = 1L)

        verify(exactly = 1) { syncScheduler.scheduleSync() }
    }

    @Test
    fun `savePartialBox schedules sync on success`() = runTest {
        val fakeBoxEntity = fakeBoxEntity()
        coEvery { boxDao.getBoxById(1L) } returns fakeBoxEntity

        repository.savePartialBox(boxId = 1L)

        verify(exactly = 1) { syncScheduler.scheduleSync() }
    }

    @Test
    fun `finalizeBox does not schedule sync on failure`() = runTest {
        coEvery { boxDao.getBoxById(1L) } throws Exception("Banco corrompido")

        val result = repository.finalizeBox(boxId = 1L)

        verify(exactly = 0) { syncScheduler.scheduleSync() }
        assertEquals(true, result.isFailure)
    }
}
