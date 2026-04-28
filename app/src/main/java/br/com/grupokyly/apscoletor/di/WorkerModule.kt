package br.com.grupokyly.apscoletor.di

import androidx.hilt.work.HiltWorkerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    // Actually, HiltWorkerFactory is automatically provided by Hilt if we add @HiltWorker on workers
    // and the androidx.hilt:hilt-work dependency.
    // However, sometimes it's explicitly needed or people just inject it directly in Application.
    // The prompt says:
    // WorkerModule.kt (di/)
    // - Configurar HiltWorkerFactory no ApsColetorApplication
    // - Registrar SyncPickingWorker para injeção via @HiltWorkerFactory
    
    // We don't need to manually provide HiltWorkerFactory if we use the @HiltWorker annotation
    // but the prompt demands the creation of this module. We can leave it as a placeholder
    // or just mark it as installed. The main config is in ApsColetorApplication.
}
