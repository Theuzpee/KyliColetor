package br.com.grupokyly.apscoletor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.grupokyly.apscoletor.data.local.entity.ScannedPieceEntity

@Dao
interface ScannedPieceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(piece: ScannedPieceEntity): Long

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM scanned_pieces sp 
            INNER JOIN picking_items pi ON sp.pickingItemId = pi.id 
            WHERE sp.barcode = :barcode AND pi.boxId = :boxId
        )
    """)
    suspend fun existsByBarcode(barcode: String, boxId: Long): Boolean

    @Query("SELECT * FROM scanned_pieces WHERE pickingItemId = :pickingItemId")
    suspend fun getByPickingItem(pickingItemId: Long): List<ScannedPieceEntity>
}
