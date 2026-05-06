package br.com.grupokyly.apscoletor.presentation.picking

import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.model.PickingItem
import br.com.grupokyly.apscoletor.domain.model.SkipReason

sealed class PickingUiState {
    object Idle : PickingUiState()
    
    object LoadingBox : PickingUiState()
    
    data class Collecting(
        val box: Box,
        val currentItem: PickingItem,
        val currentItemIndex: Int,
        val collectedCount: Int,
        val totalItems: Int,
        val lastScannedItems: List<br.com.grupokyly.apscoletor.domain.model.ScannedPreview> = emptyList(),
        val isMultiFloor: Boolean = false,
        val floorLabel: String = "",
        val addressConfirmation: br.com.grupokyly.apscoletor.domain.model.AddressConfirmationState = br.com.grupokyly.apscoletor.domain.model.AddressConfirmationState.Pending
    ) : PickingUiState()
    
    data class ItemComplete(
        val box: Box,
        val completedItem: PickingItem,
        val nextItem: PickingItem?
    ) : PickingUiState()
    
    data class ItemSkipped(
        val skippedItem: PickingItem,
        val reason: SkipReason,
        val nextItem: PickingItem?
    ) : PickingUiState()
    
    data class BoxFinalized(val box: Box) : PickingUiState()
    
    data class BoxPartial(val box: Box) : PickingUiState()
    
    data class BoxMultiFloor(
        val box: Box,
        val collectedInThisFloor: Int,
        val totalPending: Int
    ) : PickingUiState()
    
    data class Error(val message: String) : PickingUiState()
}
