package br.com.grupokyly.apscoletor.di

import br.com.grupokyly.apscoletor.domain.validator.AddressValidatorImpl
import br.com.grupokyly.apscoletor.domain.validator.AddressValidator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ValidatorModule {

    @Binds
    abstract fun bindAddressValidator(
        impl: AddressValidatorImpl
    ): AddressValidator
}
