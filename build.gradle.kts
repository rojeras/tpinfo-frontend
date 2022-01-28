import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

/*
    Temporary fix to ensure a node version which is supported for MAC computers
    Refer to https://youtrack.jetbrains.com/issue/KT-49109#focus=Comments-27-5259190.0-0

    LEO 2022-01-28
 */
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = "16.0.0"
}
// End of temporary fix

plugins {
    val kotlinVersion: String by System.getProperties()
    val kvisionVersion: String by System.getProperties()
    val dokkaVersion: String by System.getProperties()
    // val snabbdomKotlinVersion: String by System.getProperties()
    kotlin("plugin.serialization") version kotlinVersion
    kotlin("js") version kotlinVersion
    id("io.kvision") version kvisionVersion
    id("org.jetbrains.dokka") version dokkaVersion
}

version = "1.0.0-SNAPSHOT"
group = "com.example"

repositories {
    mavenCentral()
    mavenLocal()
}

// Versions
val kotlinVersion: String by System.getProperties()
val kvisionVersion: String by System.getProperties()

// Custom Properties
val webDir = file("src/main/web")

kotlin {
    js {
        browser {
            runTask {
                outputFileName = "main.bundle.js"
                sourceMaps = false
                devServer = KotlinWebpackConfig.DevServer(
                    open = false,
                    port = 2000,
                    proxy = mutableMapOf(
                        "/kv/*" to "http://localhost:8080",
                        "/kvws/*" to mapOf("target" to "ws://localhost:8080", "ws" to true)
                    ),
                    static = mutableListOf("$buildDir/processedResources/js/main")
                )
            }
            // Added webpackTask migrating to KVision 3.12.0
            webpackTask {
                outputFileName = "main.bundle.js"
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
    }
    sourceSets["main"].dependencies {
        implementation(npm("react-awesome-button", "*"))
        implementation(npm("file-saver", "2.0.2"))
        implementation("io.kvision:kvision:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap-css:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap-datetime:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap-select:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap-spinner:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap-upload:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap-dialog:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap-typeahead:$kvisionVersion")
        implementation("io.kvision:kvision-tabulator:$kvisionVersion")
        implementation("io.kvision:kvision-datacontainer:$kvisionVersion")
        implementation("io.kvision:kvision-redux-kotlin:$kvisionVersion")
        implementation("io.kvision:kvision-pace:$kvisionVersion")
        implementation("io.kvision:kvision-routing-navigo:$kvisionVersion")
        implementation("io.kvision:kvision-fontawesome:$kvisionVersion")
        implementation("io.kvision:kvision-richtext:$kvisionVersion")
        implementation("io.kvision:kvision-chart:$kvisionVersion")
    }
    sourceSets["test"].dependencies {
        implementation(kotlin("test-js"))
        implementation("io.kvision:kvision-testutils:$kvisionVersion:tests")
    }
    sourceSets["main"].resources.srcDir(webDir)
}

tasks.dokkaHtml.configure {
    val USER_HOME = System.getenv("HOME")
    // Set module name displayed in the final output
    moduleName.set("tpinfo-frontend")
    outputDirectory.set(buildDir.resolve("dokka"))
    cacheRoot.set(file("$USER_HOME/.cache/dokka"))
    dokkaSourceSets.configureEach {
        includes.from("src/main/kotlin/se/skoview/MODULE.md")
        includes.from("src/main/kotlin/se/skoview/controller/CONTROLLER.PACKAGE.md")
        includes.from("src/main/kotlin/se/skoview/model/MODEL.PACKAGE.md")
        includes.from("src/main/kotlin/se/skoview/view/hippo/HIPPO.PACKAGE.md")
        includes.from("src/main/kotlin/se/skoview/view/stat/STAT.PACKAGE.md")
    }
}