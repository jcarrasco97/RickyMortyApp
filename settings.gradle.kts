pluginManagement {
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
    // Configuración de repositorios para las dependencias del proyecto.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // Repositorio JitPack.
        // Necesario específicamente para descargar la librería de gráficos MPAndroidChart,
        // ya que no se encuentra en los repositorios estándar de Google o Maven Central.
        maven { url = uri("https://jitpack.io") }
    }
}

// Nombre del proyecto y módulos incluidos.
rootProject.name = "RickyMortyApp"
include(":app")