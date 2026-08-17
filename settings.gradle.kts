rootProject.name = "kei_1111"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":app:webApp")

include(":app:core:api")
include(":app:core:common")
include(":app:core:data")
include(":app:core:designsystem")
include(":app:core:domain")
include(":app:core:local")
include(":app:core:mvi")
include(":app:core:navigation")
include(":app:core:testing")
include(":app:core:ui")
include(":app:core:utils")

include(":shared:model")

include(":server")

include(":app:feature:profile")
include(":app:feature:splash")

include(":template")

include(":test:architecture")
include(":test:e2e")
include(":test:tags")
