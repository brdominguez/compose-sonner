import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Base64
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.compose.compiler)
    id("signing")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        groupId = "io.github.brdominguez",
        artifactId = "compose-sonner",
        version = "0.4.1"
    )

    pom {
        name.set("compose-sonner")
        description.set("An opinionated toast component for Compose Multiplatform.")
        inceptionYear.set("2025")
        url.set("https://github.com/brdominguez/compose-sonner")
        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("brdominguez")
                name.set("Bryan Dominguez")
                url.set("https://github.com/brdominguez/")
            }
        }
        scm {
            url.set("https://github.com/brdominguez/compose-sonner")
            connection.set("scm:git:git://github.com/brdominguez/compose-sonner.git")
            developerConnection.set("scm:git:ssh://git@github.com/brdominguez/compose-sonner.git")
        }

        // https://github.com/vanniktech/gradle-maven-publish-plugin/issues/802
        withXml {
            val repo = asNode().appendNode("repositories").appendNode("repository")
            repo.appendNode("name", "Google")
            repo.appendNode("id", "google")
            repo.appendNode("url", " https://maven.google.com/")
        }
    }
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        compilerOptions {
            outputModuleName.set("compose-sonner")
        }
        browser {
            commonWebpackConfig {
                outputFileName = "compose-sonner.js"
            }

            testTask {
                // Tests are broken now: Module not found: Error: Can't resolve './skiko.mjs'
                enabled = false
            }
        }
        binaries.library()
    }
    js(IR) {
        compilerOptions {
            outputModuleName.set("compose-sonner-jscanvas")
        }
        browser {
            commonWebpackConfig {
                outputFileName = "compose-sonner-jscanvas.js"
            }

            testTask {
                // Tests are broken now: Module not found: Error: Can't resolve './skiko.mjs'
                enabled = false
            }
        }
        binaries.library()
    }

    androidLibrary {
        namespace = "com.dokar.sonner.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "sonner"
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.coroutines.core)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        val desktopTest by getting {
            dependencies {
                implementation(libs.compose.ui.test.junit4)
            }
        }
    }
}

tasks.named("iosSimulatorArm64Test") {
    enabled = false
}

tasks
    .withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>()
    .configureEach {
        compilerOptions
            .jvmTarget
            .set(JvmTarget.JVM_11)
    }