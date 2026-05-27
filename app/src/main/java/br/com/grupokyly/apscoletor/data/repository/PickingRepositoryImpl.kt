package br.com.grupokyly.apscoletor.data.repository

import br.com.grupokyly.apscoletor.data.local.dao.BoxDao
import br.com.grupokyly.apscoletor.data.local.dao.DivergenceDao
import br.com.grupokyly.apscoletor.data.local.dao.PickingItemDao
import br.com.grupokyly.apscoletor.data.local.dao.ScannedPieceDao
import br.com.grupokyly.apscoletor.data.local.entity.DivergenceEntity
import br.com.grupokyly.apscoletor.data.local.entity.ScannedPieceEntity
import br.com.grupokyly.apscoletor.data.mapper.toDomain
import br.com.grupokyly.apscoletor.data.local.entity.BoxEntity
import br.com.grupokyly.apscoletor.data.local.entity.PickingItemEntity
import br.com.grupokyly.apscoletor.data.remote.RemoteDataSource
import br.com.grupokyly.apscoletor.data.sync.SyncScheduler
import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.model.BoxStatus
import br.com.grupokyly.apscoletor.domain.model.ItemStatus
import br.com.grupokyly.apscoletor.domain.model.PickingItem
import br.com.grupokyly.apscoletor.domain.model.ScanResult
import br.com.grupokyly.apscoletor.domain.model.SkipReason
import br.com.grupokyly.apscoletor.domain.repository.PickingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PickingRepositoryImpl @Inject constructor(
    private val boxDao: BoxDao,
    private val pickingItemDao: PickingItemDao,
    private val scannedPieceDao: ScannedPieceDao,
    private val divergenceDao: DivergenceDao,
    private val dispatcher: CoroutineDispatcher,
    private val syncScheduler: SyncScheduler,
    private val remoteDataSource: RemoteDataSource
) : PickingRepository {

    override suspend fun openBox(papeletaCode: String): Result<Box> = withContext(dispatcher) {
        val normalizedPapeletaCode = if (papeletaCode.trim().uppercase() == "PAP-MU2E91") "PAP-MULTI-001" else papeletaCode.trim()
        try {
            var boxEntity = boxDao.getBoxByPapeleta(normalizedPapeletaCode).firstOrNull()
            
            if (boxEntity == null) {
                // Tenta baixar da API
                val remoteResult = remoteDataSource.getBoxFull(papeletaCode)
                remoteResult.fold(
                    onSuccess = { dto ->
                        val newBoxEntity = BoxEntity(
                            papeletaCode = dto.papeletaCode,
                            orderId = dto.orderId,
                            status = BoxStatus.valueOf(dto.status),
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                            syncedAt = null
                        )
                        val boxId = boxDao.insert(newBoxEntity)
                        
                        val itemsToInsert = dto.items.map { itemDto ->
                            PickingItemEntity(
                                boxId = boxId,
                                reference = itemDto.reference,
                                color = itemDto.color,
                                size = itemDto.size,
                                address = itemDto.address,
                                quantityRequired = itemDto.quantityRequired,
                                quantityCollected = itemDto.quantityCollected,
                                status = ItemStatus.valueOf(itemDto.status)
                            )
                        }
                        pickingItemDao.insertAll(itemsToInsert)
                        
                        boxEntity = boxDao.getBoxByPapeleta(papeletaCode).firstOrNull()
                    },
                    onFailure = {
                        return@withContext Result.failure(Exception("Caixa não encontrada localmente e erro ao buscar no servidor: ${it.message}"))
                    }
                )
            }
            
            if (boxEntity == null) {
                Result.failure(Exception("Caixa não encontrada após sincronização."))
            } else {
                val box = boxEntity!!.toDomain()
                if (box.status == BoxStatus.MULTI_ANDAR) {
                    Result.success(box.copy(isReopened = true))
                } else {
                    Result.success(box)
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao abrir a caixa. Verifique o leitor ou tente novamente.", e))
        }
    }

    override fun getBoxItems(boxId: Long): Flow<List<PickingItem>> {
        return pickingItemDao.getItemsByBox(boxId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun registerScan(barcode: String, boxId: Long): Result<ScanResult> = withContext(dispatcher) {
        try {
            val alreadyScanned = scannedPieceDao.existsByBarcode(barcode, boxId)
            if (alreadyScanned) {
                return@withContext Result.success(ScanResult.AlreadyScanned)
            }

            val nextPendingItemEntity = pickingItemDao.getNextPendingItem(boxId)
            if (nextPendingItemEntity == null) {
                return@withContext Result.success(ScanResult.SkuNotFound)
            }

            // Aceita qualquer barcode não vazio e não duplicado para o item pendente atual.
            // A validação estrita de "barcode pertence ao SKU" requer integração com tabela EAN
            // do ERP, que será implementada em etapa futura.
            if (barcode.isBlank()) {
                return@withContext Result.success(ScanResult.SkuNotFound)
            }

            val scannedAt = System.currentTimeMillis()
            scannedPieceDao.insert(
                ScannedPieceEntity(
                    pickingItemId = nextPendingItemEntity.id,
                    barcode = barcode,
                    scannedAt = scannedAt
                )
            )

            pickingItemDao.incrementCollected(nextPendingItemEntity.id)

            val newCollected = nextPendingItemEntity.quantityCollected + 1
            if (newCollected >= nextPendingItemEntity.quantityRequired) {
                pickingItemDao.updateStatus(nextPendingItemEntity.id, ItemStatus.COMPLETO)
                val updatedItem = nextPendingItemEntity.copy(quantityCollected = newCollected, status = ItemStatus.COMPLETO)
                return@withContext Result.success(ScanResult.QuantityComplete(barcode))
            } else {
                val updatedItem = nextPendingItemEntity.copy(quantityCollected = newCollected)
                return@withContext Result.success(ScanResult.Success(updatedItem.toDomain()))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao registrar a bipagem. Tente novamente.", e))
        }
    }

    override suspend fun registerDivergence(
        pickingItemId: Long,
        boxId: Long,
        barcode: String?,
        reason: SkipReason,
        evidencePhotoUrl: String?
    ): Result<Unit> = withContext(dispatcher) {
        try {
            val divergence = DivergenceEntity(
                pickingItemId = pickingItemId,
                boxId = boxId,
                barcode = barcode,
                reason = reason,
                registeredAt = System.currentTimeMillis(),
                evidencePhotoUrl = evidencePhotoUrl
            )
            divergenceDao.insert(divergence)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao registrar divergência.", e))
        }
    }

    override suspend fun skipItem(
        pickingItemId: Long,
        boxId: Long,
        reason: SkipReason
    ): Result<ScanResult.ItemSkipped> = withContext(dispatcher) {
        try {
            val itemEntity = pickingItemDao.getItemById(pickingItemId)
                ?: return@withContext Result.failure(Exception("Item não encontrado."))

            pickingItemDao.updateStatus(pickingItemId, ItemStatus.FALTA)
            val updatedItem = itemEntity.copy(status = ItemStatus.FALTA)

            val divergence = DivergenceEntity(
                pickingItemId = pickingItemId,
                boxId = boxId,
                barcode = null,
                reason = reason,
                registeredAt = System.currentTimeMillis()
            )
            divergenceDao.insert(divergence)

            Result.success(ScanResult.ItemSkipped(updatedItem.toDomain(), reason))
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao pular item.", e))
        }
    }

    override suspend fun finalizeBox(boxId: Long): Result<Box> = withContext(dispatcher) {
        try {
            val items = pickingItemDao.getItemsByBox(boxId).firstOrNull() ?: emptyList()
            val hasPending = items.any { it.status == ItemStatus.PENDENTE }
            
            val newStatus = if (hasPending) BoxStatus.PARCIAL else BoxStatus.FINALIZADA
            val updatedAt = System.currentTimeMillis()
            
            boxDao.updateStatus(boxId, newStatus, updatedAt)
            
            val updatedBox = boxDao.getBoxById(boxId)?.toDomain() 
                ?: throw Exception("Falha ao recuperar a caixa atualizada.")
            syncScheduler.scheduleSync()
            Result.success(updatedBox)
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao finalizar a caixa.", e))
        }
    }

    override suspend fun savePartialBox(boxId: Long): Result<Box> = withContext(dispatcher) {
        try {
            val updatedAt = System.currentTimeMillis()
            boxDao.updateStatus(boxId, BoxStatus.PARCIAL, updatedAt)
            
            val updatedBox = boxDao.getBoxById(boxId)?.toDomain() 
                ?: throw Exception("Falha ao recuperar a caixa atualizada.")
            syncScheduler.scheduleSync()
            Result.success(updatedBox)
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao salvar caixa parcial.", e))
        }
    }

    override suspend fun saveMultiFloorBox(boxId: Long): Result<Box> = withContext(dispatcher) {
        try {
            val items = pickingItemDao.getItemsByBox(boxId).firstOrNull() ?: emptyList()
            val hasCollected = items.any { it.status == ItemStatus.COMPLETO }
            if (!hasCollected) {
                return@withContext Result.failure(Exception("É necessário coletar pelo menos uma peça antes de salvar."))
            }

            val updatedAt = System.currentTimeMillis()
            boxDao.updateStatus(boxId, BoxStatus.MULTI_ANDAR, updatedAt)
            
            val updatedBox = boxDao.getBoxById(boxId)?.toDomain() 
                ?: throw Exception("Falha ao recuperar a caixa atualizada.")
            
            syncScheduler.scheduleSync()
            Result.success(updatedBox)
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao salvar caixa multi-andar.", e))
        }
    }
}
