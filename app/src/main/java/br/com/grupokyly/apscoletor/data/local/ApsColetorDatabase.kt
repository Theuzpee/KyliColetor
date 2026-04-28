package br.com.grupokyly.apscoletor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.com.grupokyly.apscoletor.data.local.dao.BoxDao
import br.com.grupokyly.apscoletor.data.local.dao.PickingItemDao
import br.com.grupokyly.apscoletor.data.local.dao.ScannedPieceDao
import br.com.grupokyly.apscoletor.data.local.entity.BoxEntity
import br.com.grupokyly.apscoletor.data.local.entity.PickingItemEntity
import br.com.grupokyly.apscoletor.data.local.entity.ScannedPieceEntity

@Database(
    entities = [
        BoxEntity::class,
        PickingItemEntity::class,
        ScannedPieceEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ApsColetorDatabase : RoomDatabase() {
    abstract fun boxDao(): BoxDao
    abstract fun pickingItemDao(): PickingItemDao
    abstract fun scannedPieceDao(): ScannedPieceDao
}
