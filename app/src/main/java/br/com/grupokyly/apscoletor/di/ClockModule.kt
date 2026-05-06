package br.com.grupokyly.apscoletor.di

import br.com.grupokyly.apscoletor.util.Clock
import br.com.grupokyly.apscoletor.util.SystemClock
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ClockModule {

    @Binds
    abstract fun bindClock(systemClock: SystemClock): Clock
}
