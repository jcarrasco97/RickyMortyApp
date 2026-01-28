plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
    // Pluguin Parcelize para poder enviar episodios como paquetes
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.rickymortyapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.rickymortyapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // 1. NAVEGACIÓN (Para el menú lateral y fragments)
    val navVersion = "2.7.7"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

    // 2. RETROFIT & GSON (Para conectar con la API de Rick y Morty)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // 3. GLIDE (Para cargar las imágenes de los personajes)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // 4. FIREBASE (Plataforma BOM - gestiona las versiones por mi)
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")      // Autenticación
    implementation("com.google.firebase:firebase-firestore") // Base de datos

    // 5. GRÁFICOS (MPAndroidChart para las estadísticas)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")


    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.8.0"))


    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    // https://firebase.google.com/docs/android/setup#available-libraries
}