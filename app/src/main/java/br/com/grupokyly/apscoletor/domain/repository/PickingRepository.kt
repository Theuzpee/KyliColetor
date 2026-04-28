package br.com.grupokyly.apscoletor.domain.repository

import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.model.PickingItem
import br.com.grupokyly.apscoletor.domain.model.ScanResult
import kotlinx.coroutines.flow.Flow

interface PickingRepository {
    suspend fun openBox(papeletaCode: String): Result<Box>
    fun getBoxItems(boxId: Long): Flow<List<PickingItem>>
    suspend fun registerScan(barcode: String, boxId: Long): Result<ScanResult>
    suspend fun finalizeBox(boxId: Long): Result<Box>
    suspend fun savePartialBox(boxId: Long): Result<Box>
}
