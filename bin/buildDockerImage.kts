#!/usr/bin/env kscript

import java.lang.Exception
import kotlin.system.exitProcess

// my imports

//INCLUDE ./LeoLib.kts
// String.exec(currentDir)

// -------------------------------------------------------------------------------------------
// Main program

Argument.scriptDesc = "This script builds a hippo frontend in a Docker image"
Argument("all", "aaa is used to ...", true)
Argument("ball", "bbb is used to ...", false, "path")

Argument.parse(args)

/*
val usage = """
    Usage:
        -a, --all: Bla bla bla 
""".trimIndent()
*/






println("Arg parsing succesful")

println(Argument.arguments)
println("File spec: '${Argument.fileSpec}'")

val fileList2 = "ls -la".exec()
println(fileList2)