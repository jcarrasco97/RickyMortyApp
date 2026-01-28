// Archivo de construcción de nivel superior.
// Aquí se añaden opciones de configuración comunes a todos los subproyectos/módulos.
plugins {
    // Plugins base de Android y Kotlin. 'apply false' indica que no se aplican al proyecto raíz directamente,
    // sino que estarán disponibles para los módulos (como :app).
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Dependencia del plugin de Google Services.
    // Esencial para que la app pueda comunicarse con los servicios de Firebase.
    id("com.google.gms.google-services") version "4.4.4" apply false
}