import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

val kotlin_version: String by project
val ktor_version: String by project

plugins {
    kotlin("multiplatform") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
}

group = "com.github.andreyjpeg"
version = "0.1.0"

kotlin {
    applyDefaultHierarchyTemplate()

    macosX64()
    macosArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.github.ajalt.clikt:clikt:5.0.3")
                implementation("io.ktor:ktor-client-core:$ktor_version")
                implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val nativeMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktor_version")
            }
        }
        val appleMain by getting
        val appleTest by getting
    }

    targets.withType<KotlinNativeTarget>().configureEach {
        binaries {
            executable(listOf(DEBUG)) {
                baseName = "listen"
                entryPoint = "com.github.andreyjpeg.listen.main"
            }
        }
    }
}

kotlin.jvmToolchain(21)
