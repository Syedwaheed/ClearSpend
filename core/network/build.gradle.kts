plugins {
    alias(libs.plugins.clearspend.android.data)
    alias(libs.plugins.clearspend.jvm.ktor)
}

android {
    namespace = "com.edu.core.network"


}

dependencies {
    implementation(projects.core.domain)
    implementation(libs.timber)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)
}