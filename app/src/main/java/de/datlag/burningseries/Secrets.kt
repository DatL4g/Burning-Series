package de.datlag.burningseries

import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class Secrets {

    //Method calls will be added by gradle task hideSecret
    //Example : external fun getWellHiddenSecret(packageName: String): String

    companion object {
        init {
            System.loadLibrary("secrets")
        }
    }

    external fun getMALClientId(packageName: String): String

    external fun getAniListClientId(packageName: String): String

    external fun getAniListClientSecret(packageName: String): String

    external fun getGitHubClientId(packageName: String): String

    external fun getGitHubClientSecret(packageName: String): String
}