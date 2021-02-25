pluginManagement {
    repositories {
        maven(url = "https://artifactory.allegrogroup.com/artifactory/group-allegro/")
        maven(url = "https://artifactory.allegrogroup.com/artifactory/remote-repos/")
        gradlePluginPortal()
    }
}

rootProject.name = "graphql-audit"

include("graphql-audit")
