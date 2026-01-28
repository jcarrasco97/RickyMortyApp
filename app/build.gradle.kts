plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // Plugin de Google Services para inicializar Firebase en este módulo.
    id("com.google.gms.google-services")

    // Plugin Parcelize.
    // Permite serializar objetos complejos (como la clase Episode) de manera eficiente
    // para pasarlos entre Fragments mediante Bundles.
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.rickymortyapp"
    compileSdk = 34 // Versión del SDK con la que se compila el código (ajustado a estándar actual)

    defaultConfig {
        applicationId = "com.example.rickymortyapp"
        minSdk = 24 // Android 7.0. Garantiza compatibilidad con la mayoría de dispositivos modernos.
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Configuración para la versión de producción.
            // isMinifyEnabled = false para facilitar la depuración en esta fase académica.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Compatibilidad con Java 11, requerido por las versiones recientes de las librerías de Android.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Librerías base de Android (Core, UI, Activity).
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Librerías de testing (Unitarias e Instrumentales).
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // 1. NAVEGACIÓN (Jetpack Navigation Component)
    // Gestiona el flujo entre Fragments y el menú lateral (Drawer).
    val navVersion = "2.7.7"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

    // 2. RETROFIT & GSON
    // Retrofit: Cliente HTTP para realizar peticiones a la API REST de Rick and Morty.
    // Gson: Convertidor para transformar el JSON de respuesta en objetos Kotlin automáticamente.
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // 3. GLIDE
    // Librería de gestión de imágenes. Se encarga de descargar, cachear y mostrar
    // las fotos de los personajes de forma asíncrona para no bloquear la UI.
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // 4. FIREBASE (BOM - Bill of Materials)
    // El BOM gestiona las versiones de todas las librerías de Firebase para asegurar compatibilidad.
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))

    // Analytics: Para métricas de uso (opcional pero incluido por defecto).
    implementation("com.google.firebase:firebase-analytics")
    // Auth: Gestión de usuarios (Login, Registro, Sesión).
    implementation("com.google.firebase:firebase-auth")
    // Firestore: Base de datos NoSQL en la nube para guardar los episodios vistos.
    implementation("com.google.firebase:firebase-firestore")

    // 5. GRÁFICOS (MPAndroidChart)
    // Librería externa para generar el gráfico circular de estadísticas de visualización.
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}