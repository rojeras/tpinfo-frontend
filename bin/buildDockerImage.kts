#!/usr/bin/env kscript

import java.nio.file.DirectoryIteratorException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

// my imports

//INCLUDE ./LeoLib.kts

// -------------------------------------------------------------------------------------------
// Main program

Argument.initialise("This script builds a hippo frontend in a Docker image", true, "basedir")
Argument("zipfile", "Name of build zip file", true, "file")
Argument.parse(args)

val zipDir = Directory()
println("'$zipDir'")
zipDir.cd(Argument.fileSpec)
//println(Argument.arguments)
//println("File spec: '${Argument.fileSpec}'")

val fileList2 = "ls -la".exec(zipDir.toString())
println(fileList2)