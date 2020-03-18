// Kotlin script library

import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.TimeUnit
import java.nio.file.Paths
import kotlin.system.exitProcess

// --------------------------------------------------------------------------------------------------------------
// Exececute a command
fun String.exec(inDir: String = ""): String? {
    try {
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
// Parse cmd line arguments
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
        val arguments = hashMapOf<String, Argument>()
        var fileSpec = ""
        var scriptDesc: String = "Usage..."
        fun showUsageAndExit(msg: String = "") {
            println(msg + "\n")
            println(scriptDesc)
            var usageText = "Usage: script_name "

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

            for (x in args) println(x)

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
                            if (argument.argumentName.isNotEmpty()) args[++currentArg]
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
                // Check if last arg which should interpreted as a file spec

            }

            // Verify that all mandatory arguments are specified
            for ((key, arg) in Argument.arguments) {
                if (arg.isMandatory && !arg.isSpecified) Argument.showUsageAndExit("Mandatory argument is missing: '${arg.long}'")
            }
        }
    }
}


// --------------------------------------------------------------------------------------------------------------
