package br.com.grupokyly.apscoletor.presentation.picking

import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.model.PickingItem
import br.com.grupokyly.apscoletor.domain.model.SkipReason

sealed class PickingUiState {
    object Idle : PickingUiState()
    
    object LoadingBox : PickingUiState()
    
    data class BoxResuming(
        val box: Box,
        val collectedItemsCount: Int,
        val pendingItemsCount: Int,
        val divergencesCount: Int,
        val nextAddress: String,
        val isMultiFloor: Boolean
    ) : PickingUiState()
    
    data class Collecting(
        val box: Box,
        val currentItem: PickingItem,
        val currentItemIndex: Int,
        val collectedCount: Int,
        val totalItems: Int,
        val lastScannedItems: List<br.com.grupokyly.apscoletor.domain.model.ScannedPreview> = emptyList(),
        val isMultiFloor: Boolean = false,
        val floorLabel: String = "",
        val addressConfirmation: br.com.grupokyly.apscoletor.domain.model.AddressConfirmationState = br.com.grupokyly.apscoletor.domain.model.AddressConfirmationState.Pending,
        val operatorName: String = "Operador"
    ) : PickingUiState()
    
    data class ItemComplete(
        val box: Box,
        val completedItem: PickingItem,
        val nextItem: PickingItem?
    ) : PickingUiState()
    
    data class ItemSkipped(
        val box: Box,
        val skippedItem: PickingItem,
        val reason: SkipReason,
        val nextItem: PickingItem?
    ) : PickingUiState()
    
    data class BoxFinalized(
        val box: Box,
        val collectedItems: List<PickingItem> = emptyList(),
        val totalCollected: Int = 0,
        val totalRequired: Int = 0,
        val collectionTimeMinutes: Int = 0
    ) : PickingUiState()
    
    data class BoxPartial(
        val box: Box,
        val collectedItems: List<PickingItem> = emptyList(),
        val pendingItems: List<PickingItem> = emptyList(),
        val totalCollected: Int = 0,
        val totalRequired: Int = 0
    ) : PickingUiState()
    
    data class BoxMultiFloor(
        val box: Box,
        val collectedInThisFloor: Int,
        val totalPending: Int
    ) : PickingUiState()
    
    data class Error(val message: String) : PickingUiState()
}
