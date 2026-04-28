package br.com.grupokyly.apscoletor

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import br.com.grupokyly.apscoletor.data.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ApsColetorApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var syncScheduler: SyncScheduler
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
            
    override fun onCreate() {
        super.onCreate()
        syncScheduler.schedulePeriodic()
    }
}
