package de.datlag.burningseries.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.datlag.executor.Executor
import io.michaelrocks.paranoid.Obfuscate

@Module
@InstallIn(SingletonComponent::class)
@Obfuscate
object AppModule {

    @Provides
    fun provideExecutor() = Executor()
}