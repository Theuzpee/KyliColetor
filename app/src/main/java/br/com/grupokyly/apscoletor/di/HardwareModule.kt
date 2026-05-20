package br.com.grupokyly.apscoletor.di

import br.com.grupokyly.apscoletor.domain.hardware.FeedbackProvider
import br.com.grupokyly.apscoletor.hardware.ScanFeedbackManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class HardwareModule {

    @Binds
    abstract fun bindFeedbackProvider(
        impl: ScanFeedbackManager
    ): FeedbackProvider
}
