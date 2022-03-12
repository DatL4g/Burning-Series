package de.datlag.burningseries.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.datlag.executor.Executor
import io.michaelrocks.paranoid.Obfuscate
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
@Obfuscate
object AppModule {

    @Provides
    fun provideExecutor() = Executor()

    @Provides
    @Named("packageName")
    fun providePackageName(
        @ApplicationContext app: Context
    ) = app.packageName
}