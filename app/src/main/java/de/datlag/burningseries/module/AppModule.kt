package de.datlag.burningseries.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.datlag.coilifier.BlurHash
import de.datlag.executor.Executor
import io.michaelrocks.paranoid.Obfuscate
import java.io.File
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

    @Provides
    @Named("filesDir")
    fun provideFilesDir(
        @ApplicationContext app: Context
    ) = app.filesDir

    @Provides
    @Named("coversDir")
    fun provideCoversDir(
        @Named("filesDir") filesDir: File
    ) = File(filesDir, "covers")

    @Provides
    fun provideBlurHash(
        @ApplicationContext app: Context
    ) = BlurHash(app)
}