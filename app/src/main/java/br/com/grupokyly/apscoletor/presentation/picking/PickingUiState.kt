package br.com.grupokyly.apscoletor.presentation.picking

import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.model.PickingItem

sealed class PickingUiState {
    object Idle : PickingUiState()
    
    object LoadingBox : PickingUiState()
    
    data class Collecting(
        val box: Box,
        val currentItem: PickingItem,
        val currentItemIndex: Int,
        val collectedCount: Int,
        val totalItems: Int
    ) : PickingUiState()
    
    data class ItemComplete(
        val box: Box,
        val completedItem: PickingItem,
        val nextItem: PickingItem?
    ) : PickingUiState()
    
    data class BoxFinalized(val box: Box) : PickingUiState()
    
    data class BoxPartial(val box: Box) : PickingUiState()
    
    data class Error(val message: String) : PickingUiState()
}
