package br.com.grupokyly.apscoletor.di

import android.content.Context
import androidx.room.Room
import br.com.grupokyly.apscoletor.data.local.ApsColetorDatabase
import br.com.grupokyly.apscoletor.data.local.dao.BoxDao
import br.com.grupokyly.apscoletor.data.local.dao.PickingItemDao
import br.com.grupokyly.apscoletor.data.local.dao.ScannedPieceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ApsColetorDatabase {
        return Room.databaseBuilder(
            context,
            ApsColetorDatabase::class.java,
            "aps_coletor_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideBoxDao(database: ApsColetorDatabase): BoxDao = database.boxDao()

    @Provides
    fun providePickingItemDao(database: ApsColetorDatabase): PickingItemDao = database.pickingItemDao()

    @Provides
    fun provideScannedPieceDao(database: ApsColetorDatabase): ScannedPieceDao = database.scannedPieceDao()

    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
