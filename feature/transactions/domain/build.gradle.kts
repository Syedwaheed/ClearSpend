plugins {
    alias(libs.plugins.clearspend.jvm.library)
}
dependencies{
    implementation(libs.kotlinx.coroutines.core)
    implementation(projects.core.domain)

}