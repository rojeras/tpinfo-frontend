
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce

/**  Removed when migrating to KVision 3.12.0
buildscript {
 extra.set("production", (findProperty("prod") ?: findProperty("production") ?: "false") == "true")
}
*/

plugins {
    val kotlinVersion: String by System.getProperties()
    val dokkaVersion: String by System.getProperties()
    id("kotlinx-serialization") version kotlinVersion
    kotlin("js") version kotlinVersion
    id("org.jetbrains.dokka") version dokkaVersion
}

version = "1.0.0-SNAPSHOT"
group = "com.example"
// val USER_HOME = "$System.env.HOME"

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-js-wrappers") }
    maven { url = uri("https://dl.bintray.com/rjaros/kotlin") }
    mavenLocal()
}

// Versions
val kotlinVersion: String by System.getProperties()
val kvisionVersion: String by System.getProperties()
val coroutinesVersion: String by project

// Custom Properties
val webDir = file("src/main/web")
// val isProductionBuild = project.extra.get("production") as Boolean

kotlin {
    js {
        browser {
            runTask {
                outputFileName = "main.bundle.js"
                // sourceMaps = false
                devServer = KotlinWebpackConfig.DevServer(
                    open = false,
                    port = 2000,
                    proxy = mutableMapOf(
                        "/kv/*" to "http://localhost:8080",
                        "/kvws/*" to mapOf("target" to "ws://localhost:8080", "ws" to true)
                    ),
                    // contentBase = listOf("$buildDir/processedResources/js/main")
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
        implementation("io.kvision:kvision-fontawesome:$kvisionVersion")
        // implementation("io.kvision:kvision-i18n:$kvisionVersion")
        implementation("io.kvision:kvision-richtext:$kvisionVersion")
        implementation("io.kvision:kvision-datacontainer:$kvisionVersion")
        implementation("io.kvision:kvision-chart:$kvisionVersion")
        implementation("io.kvision:kvision-tabulator:$kvisionVersion")
        implementation("io.kvision:kvision-pace:$kvisionVersion")
    	implementation("io.kvision:kvision-redux-kotlin:$kvisionVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion")
        implementation("io.kvision:kvision-routing-navigo:$kvisionVersion")
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

fun getNodeJsBinaryExecutable(): String {
    // val nodeDir = NodeJsRootPlugin.apply(project).nodeJsSetupTask.destination
    val nodeDir = NodeJsRootPlugin.apply(project).nodeJsSetupTaskProvider.get().destination
    val isWindows = System.getProperty("os.name").toLowerCase().contains("windows")
    val nodeBinDir = if (isWindows) nodeDir else nodeDir.resolve("bin")
    val command = NodeJsRootPlugin.apply(project).nodeCommand
    val finalCommand = if (isWindows && command == "node") "node.exe" else command
    return nodeBinDir.resolve(finalCommand).absolutePath
}

tasks {
    withType<KotlinJsDce> {
        doLast {
            copy {
                file("$buildDir/tmp/expandedArchives/").listFiles()?.forEach {
                    if (it.isDirectory && it.name.startsWith("kvision")) {
                        from(it) {
                            include("css/**")
                            include("img/**")
                            include("js/**")
                        }
                    }
                }
                into(file("${buildDir.path}/js/packages/${project.name}/kotlin-dce"))
            }
        }
    }
    create("generatePotFile", Exec::class) {
        dependsOn("compileKotlinJs")
        executable = getNodeJsBinaryExecutable()
        args("$buildDir/js/node_modules/grunt/bin/grunt", "pot")
        inputs.files(kotlin.sourceSets["main"].kotlin.files)
        outputs.file("$projectDir/src/main/resources/i18n/messages.pot")
    }
}
afterEvaluate {
    tasks {
        getByName("processResources", Copy::class) {
            dependsOn("compileKotlinJs")
            exclude("**/*.pot")
            doLast("Convert PO to JSON") {
                destinationDir.walkTopDown().filter {
                    it.isFile && it.extension == "po"
                }.forEach {
                    exec {
                        executable = getNodeJsBinaryExecutable()
                        args(
                            "$buildDir/js/node_modules/gettext.js/bin/po2json",
                            it.absolutePath,
                            "${it.parent}/${it.nameWithoutExtension}.json"
                        )
                        println("Converted ${it.name} to ${it.nameWithoutExtension}.json")
                    }
                    it.delete()
                }
            }
        }
        create("zip", Zip::class) {
            dependsOn("browserProductionWebpack")
            group = "package"
            destinationDirectory.set(file("$buildDir/libs"))
            val distribution =
                project.tasks.getByName("browserProductionWebpack", KotlinWebpack::class).destinationDirectory!!
            from(distribution) {
                include("*.*")
            }
            from(webDir)
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            inputs.files(distribution, webDir)
            outputs.file(archiveFile)
        }
    }
}
