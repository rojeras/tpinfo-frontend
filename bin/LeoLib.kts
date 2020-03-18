// Kotlin script library

import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.TimeUnit
import java.nio.file.Paths
import kotlin.system.exitProcess

// --------------------------------------------------------------------------------------------------------------
/**
 * Execute an OS command from Kotlin code.
 *
 * exec(inDir) is an extention function to the String class.
 *
 * @param inDir the directory in which the command should be executed
 * @return the stdout from the execution
 */
fun String.exec(inDir: String = ""): String? {
    try {
        //val workingDir = if (inDir.isNotEmpty()) inDir else Paths.get("").toAbsolutePath().toString()
        val workingDir = if (inDir.isNotEmpty()) inDir else Paths.get("").toAbsolutePath().toString()
        val currentDir = File(workingDir)
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .directory(currentDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        return proc.inputStream.bufferedReader().readText()
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}

// --------------------------------------------------------------------------------------------------------------
/**
 * Provide support for command line argument in a Kotlin script
 *
 * An Argument class with a compainion object to manage arguments
 *
 * Usage:
 * 1. Initialize the script description and if file spec is manadatory: Argument.initialize("This script ...", true)
 * 2. Define for each argument: Argument(name, argument-description, is-mandatory-boolean, optional-argument-argument-name)
 * 3. Parse arguments: Argument.parse(args)
 *
 * Usage is automatically generated from above
 *
 * Argument parse result is availble in: Argument.arguments[short-name] array
 * File spec is availble in: Argument.fileSpec
 */
data class Argument(
    val long: String,
    val description: String,
    val isMandatory: Boolean,
    val argumentName: String = ""
) {
    init {
        arguments[getShort()] = this
    }

    fun getShort() = long.substring(0, 1)
    var isSpecified: Boolean = false
    var parameter: String = ""

    override fun toString(): String {
        return "[short = ${getShort()}, long = $long, description = '$description', isMandatory = $isMandatory, argumentName = '$argumentName', isSpecified = $isSpecified, parameter = '$parameter']"
    }

    companion object {

        var scriptDesc: String = "Usage..."
        var isFileSpecManadatory = false

        fun initialise(desc: String, isFileSpec: Boolean = false, inFileSpecLabel: String = "fileSpec") {
            scriptDesc = desc
            isFileSpecManadatory = isFileSpec
            fileSpecLabel = inFileSpecLabel
        }

        val arguments = hashMapOf<String, Argument>()
        var fileSpec = ""
        var fileSpecLabel = "fileSpec"

        fun showUsageAndExit(msg: String = "") {
            println(msg + "\n")
            println(scriptDesc + "\n")
            var usageText = "Usage: script_name"
            for ((short, arg) in arguments) {
                var argText = "-$short"
                argText += if (arg.argumentName.isNotEmpty()) " <${arg.argumentName}>" else ""
                argText = if (! arg.isMandatory) "[$argText]" else argText
                usageText += " $argText"
            }
            if (isFileSpecManadatory) usageText += " $fileSpecLabel"
            println(usageText)
            println()
            for ((short, arg) in arguments) {
                println("     -$short | --${arg.long} : ${arg.description} ")
            }
            println()

            //println(arguments)

            exitProcess(1)
        }

        fun findArgument(arg: String): Argument? {
            //println("findArgument called with: '$arg'")
            //println("Substring is ${arg.substring(0, 2)}")
            if (arg.startsWith("--")) {
                val short = arg.substring(2, 3)
                val long = arg.substring(2)
                val arg = arguments[short]
                if (arg!!.long.equals(long)) return arg
            } else if (arg.startsWith("-") && arg.length == 2) {
                val short = arg.substring(1, 2)
                return arguments[short]
            } else {
                showUsageAndExit("Unexpected argument: '$arg'")
                "" // Just to be able to use this assignment construct
            }
            return null
        }

        fun parse(args: Array<String>) {

            var currentArg = 0
            val lastArg = args.size - 1
            var allArg = ""

            // Go through all arguments
            while (currentArg <= lastArg) {
                try {
                    val theArg = args[currentArg]
                    // Check for last arg which might be a file spec
                    if (currentArg == lastArg && !theArg.startsWith("-")) {
                        Argument.fileSpec = theArg
                        break
                    }

                    val argument = Argument.findArgument(theArg)
                    if (argument == null) Argument.showUsageAndExit("Unexpected argument specified: '$theArg'")
                    else {
                        val param =
                            if (currentArg < lastArg && argument.argumentName.isNotEmpty()) args[++currentArg]
                            else ""
                        if (param.startsWith("-")) Argument.showUsageAndExit("A parameter must not start with a '-'. Found: '$param'")
                        argument.isSpecified = true
                        argument.parameter = param
                    }
                } catch (e: Exception) {
                    println(e)
                    Argument.showUsageAndExit("Syntax error in argument list")
                }
                currentArg++
            }

            // Verify that all mandatory arguments are specified
            for ((key, arg) in Argument.arguments) {
                if (arg.isMandatory && !arg.isSpecified) Argument.showUsageAndExit("Mandatory argument is missing: '${arg.long}'")
            }
            if (isFileSpecManadatory && fileSpec.isEmpty()) Argument.showUsageAndExit("File specification is mandatory but missing")
        }
    }
}


// --------------------------------------------------------------------------------------------------------------
/**
 * Manage information about directory path
 *
 * When a Directory is instansiated it is set to the current directory
 * cd() - used to manipulate the path (String). Give error if set to a non-existant directory
 *
 */
data class Directory(
    var directory: String = ""
) {

    init {
        pwd()
    }

    fun pwd() {
        directory = "pwd".exec()!!.trim()
    }

    fun cd(toDir: String) {
        if (toDir.equals("..")) {
            directory = directory.replaceAfterLast('/', "", directory)
        } else if (toDir.startsWith("/")) {
            directory = toDir
        } else if (!toDir.equals(".")) {
            directory += "/$toDir"
        }
        if (directory.length > 1) directory = directory.removeSuffix("/")

        if (!exists()) {
            println("*** Error, directory does not exist: '$directory'")
            exitProcess(1)
        }
    }

    fun exists(): Boolean {
        return (File(directory).isDirectory())
    }

    override fun toString(): String {
        return directory
    }
}
// --------------------------------------------------------------------------------------------------------------