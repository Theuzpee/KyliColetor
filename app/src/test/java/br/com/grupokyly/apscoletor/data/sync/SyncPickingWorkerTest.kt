package br.com.grupokyly.apscoletor.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import br.com.grupokyly.apscoletor.data.local.dao.BoxDao
import br.com.grupokyly.apscoletor.data.local.dao.PickingItemDao
import br.com.grupokyly.apscoletor.data.local.dao.ScannedPieceDao
import br.com.grupokyly.apscoletor.data.remote.ConflictException
import br.com.grupokyly.apscoletor.data.remote.NetworkException
import br.com.grupokyly.apscoletor.data.remote.RemoteDataSource
import br.com.grupokyly.apscoletor.data.remote.ServerException
import br.com.grupokyly.apscoletor.test.fake.fakeBoxEntity
import br.com.grupokyly.apscoletor.test.fake.fakeSyncResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SyncPickingWorkerTest {

    private lateinit var context: Context
    private lateinit var boxDao: BoxDao
    private lateinit var itemDao: PickingItemDao
    private lateinit var pieceDao: ScannedPieceDao
    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var worker: SyncPickingWorker

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        boxDao = mockk(relaxed = true)
        itemDao = mockk(relaxed = true)
        pieceDao = mockk(relaxed = true)
        remoteDataSource = mockk(relaxed = true)

        // Mock empty items/pieces to simplify test (unless needed for a specific validation)
        every { itemDao.getItemsForBox(any()) } returns flowOf(emptyList())
        every { pieceDao.getPiecesForItem(any()) } returns flowOf(emptyList())

        worker = TestListenableWorkerBuilder<SyncPickingWorker>(context)
            .setWorkerFactory(
                object : androidx.work.WorkerFactory() {
                    override fun createWorker(
                        appContext: Context,
                        workerClassName: String,
                        workerParameters: androidx.work.WorkerParameters
                    ): ListenableWorker {
                        return SyncPickingWorker(
                            appContext,
                            workerParameters,
                            boxDao,
                            itemDao,
                            pieceDao,
                            remoteDataSource
                        )
                    }
                }
            )
            .build() as SyncPickingWorker
    }

    @Test
    fun `worker syncs pending box and updates syncedAt`() = runTest {
        val pendingBox = fakeBoxEntity(syncedAt = null)
        every { boxDao.getAllBoxes() } returns flowOf(listOf(pendingBox))
        coEvery { remoteDataSource.syncBox(any()) } returns Result.success(fakeSyncResponse())

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        // Verify insertBox is called to update the box (with a copied box that has syncedAt != null)
        verify(exactly = 1) {
            boxDao.insertBox(match { it.id == pendingBox.id && it.syncedAt != null })
        }
    }

    @Test
    fun `worker returns retry on network failure`() = runTest {
        val pendingBox = fakeBoxEntity(syncedAt = null)
        every { boxDao.getAllBoxes() } returns flowOf(listOf(pendingBox))
        coEvery { remoteDataSource.syncBox(any()) } returns Result.failure(NetworkException("Sem conexão de rede."))

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
        verify(exactly = 0) { boxDao.insertBox(match { it.syncedAt != null }) }
    }

    @Test
    fun `worker handles 409 conflict as success and marks synced`() = runTest {
        val pendingBox = fakeBoxEntity(syncedAt = null)
        every { boxDao.getAllBoxes() } returns flowOf(listOf(pendingBox))
        coEvery { remoteDataSource.syncBox(any()) } returns Result.failure(ConflictException("Caixa já sincronizada"))

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify(exactly = 1) { boxDao.insertBox(match { it.id == pendingBox.id && it.syncedAt != null }) }
    }

    @Test
    fun `worker returns success when no pending boxes`() = runTest {
        every { boxDao.getAllBoxes() } returns flowOf(emptyList())

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 0) { remoteDataSource.syncBox(any()) }
    }

    @Test
    fun `worker continues sync after one box fails with server error`() = runTest {
        val box1 = fakeBoxEntity(syncedAt = null).copy(id = 1, papeletaCode = "PAP1")
        val box2 = fakeBoxEntity(syncedAt = null).copy(id = 2, papeletaCode = "PAP2")
        every { boxDao.getAllBoxes() } returns flowOf(listOf(box1, box2))

        coEvery { remoteDataSource.syncBox(match { it.papeletaCode == "PAP1" }) } returns Result.failure(ServerException("Erro 500"))
        coEvery { remoteDataSource.syncBox(match { it.papeletaCode == "PAP2" }) } returns Result.success(fakeSyncResponse())

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        // box2 was updated
        verify(exactly = 1) { boxDao.insertBox(match { it.id == box2.id && it.syncedAt != null }) }
        // box1 was not updated
        verify(exactly = 0) { boxDao.insertBox(match { it.id == box1.id && it.syncedAt != null }) }
    }
}
