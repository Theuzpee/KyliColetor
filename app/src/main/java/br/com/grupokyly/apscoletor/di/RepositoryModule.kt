package br.com.grupokyly.apscoletor.di

import br.com.grupokyly.apscoletor.data.repository.PickingRepositoryImpl
import br.com.grupokyly.apscoletor.domain.repository.PickingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPickingRepository(
        pickingRepositoryImpl: PickingRepositoryImpl
    ): PickingRepository
}
