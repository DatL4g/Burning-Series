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
    @Named("malClientId")
    fun provideMALClientId(
        @ApplicationContext app: Context,
        secrets: Secrets
    ): String = secrets.getMALClientId(app.packageName)

    @Provides
    @Singleton
    @Named("anilistClientId")
    fun provideAniListClientId(
        @ApplicationContext app: Context,
        secrets: Secrets
    ): String = secrets.getAniListClientId(app.packageName)

    @Provides
    @Singleton
    @Named("anilistClientSecret")
    fun provideAniListClientSecret(
        @ApplicationContext app: Context,
        secrets: Secrets
    ): String = secrets.getAniListClientSecret(app.packageName)

    @Provides
    @Singleton
    @Named("githubClientId")
    fun provideGitHubClientId(
        @ApplicationContext app: Context,
        secrets: Secrets
    ): String = secrets.getGitHubClientId(app.packageName)

    @Provides
    @Singleton
    @Named("githubClientSecret")
    fun provideGitHubClientSecret(
        @ApplicationContext app: Context,
        secrets: Secrets
    ): String = secrets.getGitHubClientSecret(app.packageName)
}