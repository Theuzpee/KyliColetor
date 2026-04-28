package br.com.grupokyly.apscoletor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.grupokyly.apscoletor.data.local.entity.BoxEntity
import br.com.grupokyly.apscoletor.domain.model.BoxStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BoxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(box: BoxEntity): Long

    @Query("UPDATE boxes SET status = :status, updatedAt = :updatedAt WHERE id = :boxId")
    suspend fun updateStatus(boxId: Long, status: BoxStatus, updatedAt: Long)

    @Query("UPDATE boxes SET syncedAt = :syncedAt WHERE id = :boxId")
    suspend fun updateSyncedAt(boxId: Long, syncedAt: Long?)

    @Query("SELECT * FROM boxes WHERE papeletaCode = :papeletaCode LIMIT 1")
    fun getBoxByPapeleta(papeletaCode: String): Flow<BoxEntity?>

    @Query("SELECT * FROM boxes WHERE id = :boxId LIMIT 1")
    suspend fun getBoxById(boxId: Long): BoxEntity?

    @Query("SELECT * FROM boxes WHERE syncedAt IS NULL")
    suspend fun getPendingSync(): List<BoxEntity>
}
