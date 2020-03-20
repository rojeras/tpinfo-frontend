#!/usr/bin/env kscript

import java.io.File
import java.nio.file.DirectoryIteratorException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

// my imports

// todo: Do not build if there are uncomitted changes
// todo: docker tag name convention needed. Should include both git branch and commit hash|

//INCLUDE ./LeoLib.kts

// -------------------------------------------------------------------------------------------
// Main program


// -------------------------------------------------------------------------------------------
Largument.initialise(
    """
    This script builds a hippo frontend in a Docker image
    It must be run from the base dir in the git project
    """.trimIndent()
)

Largument("clean", "Do a gradle clean before the build", false)
//Largument("environment", "Specify 'qa' | 'prod'", true, "environment")
Largument("nogradle", "Do NOT run gradle before the docker build", false)
Largument("push", "Push image to NoGui docker registry", false)
Largument("run", "Run the docker image", false)

//Argument("zipfile", "Name of build zip file", true, "file")
Largument.parse(args)

/*
val environment: String = Largument.parameter("environment")
//println(Largument.arguments)
if (!(environment.contains("qa") || environment.contains("prod"))) {
    Largument.showUsageAndExit("Environment must be 'qa' or 'prod' - not '$environment'")
}
*/
// -------------------------------------------------------------------------------------------
val gitBranch = lExec("git rev-parse --abbrev-ref HEAD")

val imageName = "rojeras/tpinfo-frontend:latest-$gitBranch"

val buildDirName = "build/libs"
val buildName = "showcase-1.0.0-SNAPSHOT"
val zipDirName = "$buildDirName/$buildName"
val buildZipFile = "$buildDirName/$buildName.zip"

val currentDir = lPwd()

//exitProcess(1)

// -------------------------------------------------------------------------------------------
if (Largument.isSet("clean")) lExec("./gradlew clean")

if (!Largument.isSet("nogradle")) lExec("./gradlew zip")
File(zipDirName).walkBottomUp().forEach {
    lExec("rm $it", quiet = false)
}
lExec("unzip -d $zipDirName $buildZipFile")

lExec("docker build --rm -t $imageName .")

if (Largument.isSet("push")) {
    lExec("docker tag $imageName docker-registry.centrera.se:443/sll-tpinfo/frontend:latest-$gitBranch")
    lExec("docker push docker-registry.centrera.se:443/sll-tpinfo/frontend:latest-$gitBranch")
}

if (Largument.isSet("run")) {
    lExec("docker run -d -p 8888:80 $imageName")
    println("The image is running and listen to port 8888")
}


exitProcess(0)