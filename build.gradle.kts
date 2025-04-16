plugins {
    kotlin("plugin.compose") version "2.0.0" apply false
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}


tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}