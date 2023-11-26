import org.gradle.api.Project
import java.io.File

fun Project.parentBuildDir(child: String): File {
    return this.parent?.layout?.buildDirectory?.dir(child)?.get()?.asFile ?: File(projectDir, "../build/$child")
}