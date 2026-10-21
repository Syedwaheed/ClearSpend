plugins {
    alias(libs.plugins.clearspend.android.library)
    alias(libs.plugins.clearspend.jvm.ktor)
}

android {
    namespace = "com.edu.feature.auth.data"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.koin)

    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.network)

    implementation(projects.feature.auth.domain)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.ktor.client.mock)
}
