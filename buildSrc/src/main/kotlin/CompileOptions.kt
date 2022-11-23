import org.gradle.api.JavaVersion

object CompileOptions {
	val sourceCompatibility = JavaVersion.VERSION_11
	val targetCompatibility = JavaVersion.VERSION_11
	val jvmTarget = targetCompatibility.toString()
	val kotlinJdk = when {
		targetCompatibility.isJava7 -> "-jdk7"
		targetCompatibility.isJava8 -> "-jdk8"
		else -> String()
	}
}