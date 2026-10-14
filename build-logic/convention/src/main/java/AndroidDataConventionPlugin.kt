import com.edu.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies


class AndroidDataConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("clearspend.android.library")
            }
            dependencies{
                add("implementation", libs.findBundle("koin").get())
            }
        }
    }
}