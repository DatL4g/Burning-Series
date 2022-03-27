package de.datlag.burningseries.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.datlag.burningseries.Secrets
import io.michaelrocks.paranoid.Obfuscate
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Obfuscate
object SecretModule {

    @Provides
    @Singleton
    fun provideSecrets(): Secrets = Secrets()

    @Provides
    @Singleton
    @Named("m3oToken")
    fun provideM3OToken(
        @ApplicationContext app: Context,
        secrets: Secrets
    ): String = secrets.getM3OToken(app.packageName)

    @Provides
    @Singleton
    @Named("wrapApiToken")
    fun provideWrapApiToken(
        @ApplicationContext app: Context,
        secrets: Secrets
    ): String = secrets.getWrapAPIToken(app.packageName)

    @Provides
    @Singleton
    @Named("malClientId")
    fun providedMALClientId(
        @ApplicationContext app: Context,
        secrets: Secrets
    ): String = secrets.getMALClientId(app.packageName)
}