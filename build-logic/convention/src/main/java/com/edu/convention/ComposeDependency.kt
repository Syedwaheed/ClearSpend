package com.edu.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project

fun DependencyHandlerScope.addUiLayerDependency(project: Project){
    "implementation"(project(":core:presentation:ui"))
    "implementation"(project(":core:presentation:designsystem"))

    "implementation"(project.libs.findBundle("koin.compose").get())
    "implementation"(project.libs.findBundle("compose").get())
    "debugImplementation"(project.libs.findBundle("compose.debug").get())
    "debugImplementation"(project.libs.findLibrary("androidx.compose.ui.test.manifest").get())
    "implementation"(project.libs.findLibrary("coil-compose").get())
    "androidTestImplementation"(project.libs.findLibrary("androidx.compose.ui.test.junit4").get())

    "implementation"(project.libs.findLibrary("androidx-navigation3-ui").get())
    "implementation"(project.libs.findLibrary("androidx-lifecycle-viewmodel-savedstate").get())
    "implementation"(project.libs.findLibrary("androidx-compose-material-icons-extended").get())
    "implementation"(project.libs.findLibrary("kotlinx-coroutines-core").get())

    // Every presentation module tests its ViewModels and UiMappers the same way (CLAUDE.md §14).
    "testImplementation"(project.libs.findLibrary("kotlinx-coroutines-test").get())
    "testImplementation"(project.libs.findLibrary("mockk").get())
    "testImplementation"(project.libs.findLibrary("turbine").get())
}