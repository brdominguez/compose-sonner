import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Base64

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.compose.compiler)
    id("signing")
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(
        groupId = "io.github.brdominguez",
        artifactId = "compose-sonner",
        version = "0.3.10"
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
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "compose-sooner"
        browser {
            commonWebpackConfig {
                outputFileName = "compose-sooner.js"
            }

            testTask {
                // Tests are broken now: Module not found: Error: Can't resolve './skiko.mjs'
                enabled = false
            }
        }
        binaries.library()
    }
    js(IR) {
        moduleName = "compose-sooner-jscanvas"
        browser {
            commonWebpackConfig {
                outputFileName = "compose-sooner-jscanvas.js"
            }

            testTask {
                // Tests are broken now: Module not found: Error: Can't resolve './skiko.mjs'
                enabled = false
            }
        }
        binaries.library()
    }

    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
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
                implementation(compose.desktop.uiTestJUnit4)
            }
        }
    }
}

android {
    namespace = "com.dokar.sonner.core"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

tasks
    .withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>()
    .configureEach {
        compilerOptions
            .jvmTarget
            .set(JvmTarget.JVM_11)
    }
