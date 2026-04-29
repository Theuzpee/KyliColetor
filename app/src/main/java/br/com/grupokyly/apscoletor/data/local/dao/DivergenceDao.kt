package br.com.grupokyly.apscoletor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.grupokyly.apscoletor.data.local.entity.DivergenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DivergenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(divergence: DivergenceEntity): Long

    @Query("SELECT * FROM divergences WHERE boxId = :boxId")
    fun getByBox(boxId: Long): Flow<List<DivergenceEntity>>

    @Query("SELECT * FROM divergences WHERE syncedAt IS NULL")
    fun getPendingSync(): Flow<List<DivergenceEntity>>

    @Query("UPDATE divergences SET syncedAt = :syncedAt WHERE id = :id")
    suspend fun updateSyncedAt(id: Long, syncedAt: Long)
}
