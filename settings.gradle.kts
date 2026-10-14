enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ClearSpend"
include(":app")



include(":core:presentation:designsystem")
include(":core:presentation:ui")
include(":core:data")
include(":core:database")
include(":core:network")
include(":core:domain")
include(":feature:auth:domain")
include(":feature:auth:data")
include(":feature:auth:presentation")
include(":feature:dashboard:data")
include(":feature:dashboard:presentation")
include(":feature:dashboard:domain")
include(":feature:payments:data")
include(":feature:payments:presentation")
include(":feature:payments:domain")
include(":feature:accounts:data")
include(":feature:accounts:presentation")
include(":feature:accounts:domain")
include(":feature:transactions:data")
include(":feature:transactions:domain")
include(":feature:transactions:presentation")
include(":feature:analytics:data")
include(":feature:analytics:domain")
include(":feature:analytics:presentation")
