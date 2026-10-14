import com.edu.convention.configureKotlinJvm
import com.edu.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class JvmLibraryConventionPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        with(target){
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            configureKotlinJvm()
            dependencies {
                "testImplementation"(kotlin("test"))
                "testImplementation"(libs.findLibrary("junit").get())
                "testImplementation"(libs.findLibrary("kotlinx.coroutines.test").get())
                "testImplementation"(libs.findLibrary("mockk").get())
                "testImplementation"(libs.findLibrary("turbine").get())
            }
        }
    }
}