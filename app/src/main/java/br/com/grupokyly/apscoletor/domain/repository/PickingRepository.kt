package br.com.grupokyly.apscoletor.domain.repository

import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.model.PickingItem
import br.com.grupokyly.apscoletor.domain.model.ScanResult
import br.com.grupokyly.apscoletor.domain.model.SkipReason
import kotlinx.coroutines.flow.Flow

interface PickingRepository {
    suspend fun openBox(papeletaCode: String): Result<Box>
    fun getBoxItems(boxId: Long): Flow<List<PickingItem>>
    suspend fun registerScan(barcode: String, boxId: Long): Result<ScanResult>
    suspend fun skipItem(pickingItemId: Long, boxId: Long, reason: SkipReason): Result<ScanResult.ItemSkipped>
    suspend fun registerDivergence(pickingItemId: Long, boxId: Long, barcode: String?, reason: SkipReason, evidencePhotoUrl: String? = null): Result<Unit>
    suspend fun finalizeBox(boxId: Long): Result<Box>
    suspend fun savePartialBox(boxId: Long): Result<Box>
    suspend fun saveMultiFloorBox(boxId: Long): Result<Box>
}
