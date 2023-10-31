import org.gradle.api.JavaVersion

object CompileOptions {
    val sourceCompatibility = JavaVersion.VERSION_17
    val targetCompatibility = JavaVersion.VERSION_17
    val jvmTarget = targetCompatibility.toString()
    val kotlinJdk = when {
        targetCompatibility.isJava7 -> "-jdk7"
        targetCompatibility.isJava8 -> "-jdk8"
        else -> String()
    }
    val jvmTargetVersion = when {
        targetCompatibility.isJava5 -> 5
        targetCompatibility.isJava6 -> 6
        targetCompatibility.isJava7 -> 7
        targetCompatibility.isJava8 -> 8
        else -> targetCompatibility.majorVersion.toIntOrNull() ?: (targetCompatibility.ordinal + 1)
    }
}