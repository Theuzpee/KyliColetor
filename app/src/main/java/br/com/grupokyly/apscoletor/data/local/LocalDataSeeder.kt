package br.com.grupokyly.apscoletor.data.local

import br.com.grupokyly.apscoletor.data.local.dao.BoxDao
import br.com.grupokyly.apscoletor.data.local.dao.PickingItemDao
import br.com.grupokyly.apscoletor.data.local.entity.BoxEntity
import br.com.grupokyly.apscoletor.data.local.entity.PickingItemEntity
import br.com.grupokyly.apscoletor.domain.model.BoxStatus
import br.com.grupokyly.apscoletor.domain.model.ItemStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSeeder @Inject constructor(
    private val boxDao: BoxDao,
    private val pickingItemDao: PickingItemDao
) {
    fun seed() {
        CoroutineScope(Dispatchers.IO).launch {
            val existingBoxes = boxDao.getPendingSync()
            if (existingBoxes.isEmpty()) {
                seedBoxes()
            }
        }
    }

    private suspend fun seedBoxes() {
        // Caixa 100% finalizada (Para teste de erro/já sincronizada ou visualizar)
        insertBox(
            papeleta = "PAP-FINALIZADA-001", orderId = "PED-1001", status = BoxStatus.FINALIZADA,
            items = listOf(
                PickingItemEntity(boxId = 0, reference = "REF-1A", color = "AZUL", size = "P", address = "A01", quantityRequired = 5, quantityCollected = 5, status = ItemStatus.COMPLETO),
                PickingItemEntity(boxId = 0, reference = "REF-1B", color = "AZUL", size = "M", address = "A01", quantityRequired = 5, quantityCollected = 5, status = ItemStatus.COMPLETO),
            )
        )

        // Caixa Parcial
        insertBox(
            papeleta = "PAP-PARCIAL-001", orderId = "PED-1002", status = BoxStatus.PARCIAL,
            items = listOf(
                PickingItemEntity(boxId = 0, reference = "REF-2A", color = "PRETO", size = "M", address = "B02", quantityRequired = 10, quantityCollected = 10, status = ItemStatus.COMPLETO),
                PickingItemEntity(boxId = 0, reference = "REF-FALTA", color = "BRANCO", size = "M", address = "B03", quantityRequired = 5, quantityCollected = 0, status = ItemStatus.FALTA),
            )
        )

        // Caixa Limpa (Pronta para coleta completa do zero)
        insertBox(
            papeleta = "PAP-COLETA-001", orderId = "PED-COLETA", status = BoxStatus.EM_COLETA,
            items = listOf(
                PickingItemEntity(boxId = 0, reference = "12345", color = "VERMELHO", size = "G", address = "A05", quantityRequired = 2, quantityCollected = 0, status = ItemStatus.PENDENTE),
                PickingItemEntity(boxId = 0, reference = "67890", color = "VERMELHO", size = "M", address = "A06", quantityRequired = 1, quantityCollected = 0, status = ItemStatus.PENDENTE),
            )
        )

        // Caixa Multi-andar
        insertBox(
            papeleta = "PAP-MULTI-001", orderId = "PED-1004", status = BoxStatus.MULTI_ANDAR,
            items = listOf(
                PickingItemEntity(boxId = 0, reference = "REF-4A", color = "ROSA", size = "M", address = "D01-ANDAR-A", quantityRequired = 3, quantityCollected = 3, status = ItemStatus.COMPLETO),
                PickingItemEntity(boxId = 0, reference = "REF-4C", color = "AMARELO", size = "M", address = "Z99-ANDAR-B", quantityRequired = 5, quantityCollected = 0, status = ItemStatus.PENDENTE),
            )
        )
    }

    private suspend fun insertBox(papeleta: String, orderId: String, status: BoxStatus, items: List<PickingItemEntity>) {
        val exists = boxDao.getBoxByPapeleta(papeleta).firstOrNull()
        if (exists == null) {
            val boxEntity = BoxEntity(
                papeletaCode = papeleta,
                orderId = orderId,
                status = status,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                syncedAt = null
            )
            val boxId = boxDao.insert(boxEntity)
            
            val itemsToInsert = items.map { it.copy(boxId = boxId) }
            pickingItemDao.insertAll(itemsToInsert)
        }
    }
}
