package br.com.grupokyly.apscoletor.domain.usecase

import br.com.grupokyly.apscoletor.domain.model.PickingItem
import br.com.grupokyly.apscoletor.domain.repository.PickingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBoxItemsUseCase @Inject constructor(
    private val repository: PickingRepository
) {
    operator fun invoke(boxId: Long): Flow<List<PickingItem>> {
        return repository.getBoxItems(boxId)
    }
}
