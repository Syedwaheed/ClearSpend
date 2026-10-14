 package com.edu.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

internal fun Project.configureBuildType(
    commonExtension: CommonExtension,
    extensionType: ExtensionType
){
    when(commonExtension) {
        is ApplicationExtension -> commonExtension.buildFeatures.buildConfig = true
        is LibraryExtension -> commonExtension.buildFeatures.buildConfig = true
    }

    val localProperties = gradleLocalProperties(rootDir, providers)
    val trueLayerConfig = TrueLayerBuildConfig(
        clientId = localProperties.getProperty("TRUELAYER_CLIENT_ID").orEmpty(),
        clientSecret = localProperties.getProperty("TRUELAYER_CLIENT_SECRET").orEmpty(),
        redirectUri = localProperties.getProperty("TRUELAYER_REDIRECT_URI").orEmpty(),
        sandbox = localProperties.getProperty("TRUELAYER_SANDBOX") ?: "true",
    )
    when(extensionType){
        ExtensionType.APPLICATION ->{
            extensions.configure<ApplicationExtension>{

                buildTypes{
                    debug {
                        configureDebugBuildType(trueLayerConfig)
                    }
                    release {
                        configureReleaseBuildType(commonExtension, extensionType, trueLayerConfig)
                    }
                }
            }
        }
        ExtensionType.LIBRARY -> {
            extensions.configure<LibraryExtension>{

                buildTypes{
                    debug {
                        configureDebugBuildType(trueLayerConfig)
                    }
                    release {
                        configureReleaseBuildType(commonExtension, extensionType, trueLayerConfig)
                    }
                }
            }
        }
    }
}

internal data class TrueLayerBuildConfig(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val sandbox: String,
)

private fun BuildType.configureDebugBuildType(trueLayerConfig: TrueLayerBuildConfig){
    buildConfigField("String", "TRUELAYER_CLIENT_ID", "\"${trueLayerConfig.clientId}\"")
    buildConfigField("String", "TRUELAYER_CLIENT_SECRET", "\"${trueLayerConfig.clientSecret}\"")
    buildConfigField("String", "TRUELAYER_REDIRECT_URI", "\"${trueLayerConfig.redirectUri}\"")
    buildConfigField("boolean", "TRUELAYER_SANDBOX", trueLayerConfig.sandbox)
}
private fun BuildType.configureReleaseBuildType(
    commonExtension: CommonExtension,
    extensionType: ExtensionType,
    trueLayerConfig: TrueLayerBuildConfig,
){
    buildConfigField("String", "TRUELAYER_CLIENT_ID", "\"${trueLayerConfig.clientId}\"")
    buildConfigField("String", "TRUELAYER_CLIENT_SECRET", "\"${trueLayerConfig.clientSecret}\"")
    buildConfigField("String", "TRUELAYER_REDIRECT_URI", "\"${trueLayerConfig.redirectUri}\"")
    buildConfigField("boolean", "TRUELAYER_SANDBOX", trueLayerConfig.sandbox)

    // Library modules are never minified on their own — R8 only sees each
    // library's own narrow consumer rules in isolation and strips symbols the
    // app layer still needs (e.g. public Composables with no local keep rule).
    // Shrinking/obfuscation happens exactly once, in :app's release build.
    if (extensionType == ExtensionType.APPLICATION) {
        isMinifyEnabled = true
    }

    val proguardFile = when(commonExtension) {
        is ApplicationExtension -> commonExtension.getDefaultProguardFile("proguard-android-optimize.txt")
        is LibraryExtension -> commonExtension.getDefaultProguardFile("proguard-android-optimize.txt")
        else -> null
    }
    
    if (proguardFile != null) {
        proguardFiles(proguardFile, "proguard-rules.pro")
    } else {
        proguardFiles("proguard-rules.pro")
    }
}