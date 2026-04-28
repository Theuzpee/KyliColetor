package br.com.grupokyly.apscoletor.data.repository

import br.com.grupokyly.apscoletor.data.local.dao.BoxDao
import br.com.grupokyly.apscoletor.data.local.dao.PickingItemDao
import br.com.grupokyly.apscoletor.data.local.dao.ScannedPieceDao
import br.com.grupokyly.apscoletor.test.fake.FakeSyncScheduler
import br.com.grupokyly.apscoletor.test.fake.fakeBox
import br.com.grupokyly.apscoletor.test.fake.fakeBoxEntity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PickingRepositoryImplSyncTest {

    private lateinit var boxDao: BoxDao
    private lateinit var pickingItemDao: PickingItemDao
    private lateinit var scannedPieceDao: ScannedPieceDao
    private lateinit var fakeSyncScheduler: FakeSyncScheduler
    private lateinit var repository: PickingRepositoryImpl

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        boxDao = mockk(relaxed = true)
        pickingItemDao = mockk(relaxed = true)
        scannedPieceDao = mockk(relaxed = true)
        // Pass a mock context or application provider context since Robolectric is used
        fakeSyncScheduler = FakeSyncScheduler(ApplicationProvider.getApplicationContext())

        repository = PickingRepositoryImpl(
            boxDao = boxDao,
            pickingItemDao = pickingItemDao,
            scannedPieceDao = scannedPieceDao,
            dispatcher = testDispatcher,
            syncScheduler = fakeSyncScheduler
        )
    }

    @Test
    fun `finalizeBox schedules sync on success`() = runTest(testDispatcher) {
        val fakeBoxEntity = fakeBoxEntity()
        every { boxDao.getBoxById(1L) } returns fakeBoxEntity
        every { pickingItemDao.getItemsByBox(1L) } returns flowOf(emptyList())

        repository.finalizeBox(boxId = 1L)

        assertEquals(1, fakeSyncScheduler.scheduleCount)
    }

    @Test
    fun `savePartialBox schedules sync on success`() = runTest(testDispatcher) {
        val fakeBoxEntity = fakeBoxEntity()
        every { boxDao.getBoxById(1L) } returns fakeBoxEntity

        repository.savePartialBox(boxId = 1L)

        assertEquals(1, fakeSyncScheduler.scheduleCount)
    }

    @Test
    fun `finalizeBox does not schedule sync on failure`() = runTest(testDispatcher) {
        every { boxDao.getBoxById(1L) } throws Exception("Banco corrompido")

        val result = repository.finalizeBox(boxId = 1L)

        assertEquals(0, fakeSyncScheduler.scheduleCount)
        assertEquals(true, result.isFailure)
    }
}
