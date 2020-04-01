// Kotlin script library
/**
 * Copyright (C) 2013-2020 Lars Erik Röjerås
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.TimeUnit
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneOffset
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

fun lExec(cmd: String, inDir: String = "", quiet: Boolean = false): String? {
    if (! quiet) println(cmd + "\n" + "=".repeat(cmd.length))
    try {
        //val workingDir = if (inDir.isNotEmpty()) inDir else Paths.get("").toAbsolutePath().toString()
        val workingDir = if (inDir.isNotEmpty()) inDir else Paths.get("").toAbsolutePath().toString()
        val currentDir = File(workingDir)
        val parts = cmd.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .directory(currentDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        val result = proc.inputStream.bufferedReader().readText().trim()
        if (! quiet) println(result + "\n---------------------------------------------------------------------------")
        return result
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}

// --------------------------------------------------------------------------------------------------------------
/**
 * Provide support for command line argument in a Kotlin script
 *
 * An Largument class with a compainion object to manage arguments
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
data class Largument(
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
        val arguments = hashMapOf<String, Largument>()
        var fileSpec = ""
        var fileSpecLabel = "fileSpec"

        fun isSet(inLong: String): Boolean {
            val inShort = inLong.substring(0,1)
            return arguments[inShort]!!.isSpecified
        }

        fun parameter(inLong: String): String {
            val inShort = inLong.substring(0,1)
            return arguments[inShort]!!.parameter
        }

        fun initialise(desc: String, isFileSpec: Boolean = false, inFileSpecLabel: String = "fileSpec") {
            scriptDesc = desc
            isFileSpecManadatory = isFileSpec
            fileSpecLabel = inFileSpecLabel
        }

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

        fun findArgument(arg: String): Largument? {
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
                        Largument.fileSpec = theArg
                        break
                    }

                    val argument = Largument.findArgument(theArg)
                    if (argument == null) Largument.showUsageAndExit("Unexpected argument specified: '$theArg'")
                    else {
                        val param =
                            if (currentArg < lastArg && argument.argumentName.isNotEmpty()) args[++currentArg]
                            else ""
                        if (param.startsWith("-")) Largument.showUsageAndExit("A parameter must not start with a '-'. Found: '$param'")
                        argument.isSpecified = true
                        argument.parameter = param
                    }
                } catch (e: Exception) {
                    println(e)
                    Largument.showUsageAndExit("Syntax error in argument list")
                }
                currentArg++
            }

            // Verify that all mandatory arguments are specified
            for ((key, arg) in Largument.arguments) {
                if (arg.isMandatory && !arg.isSpecified) Largument.showUsageAndExit("Mandatory argument is missing: '${arg.long}'")
            }
            if (isFileSpecManadatory && fileSpec.isEmpty()) Largument.showUsageAndExit("File specification is mandatory but missing")
        }
    }
}


// --------------------------------------------------------------------------------------------------------------
/**
 * Useful functions
 */
//fun lPwd(): String = "pwd".exec()!!.trim()
fun lPwd(quiet: Boolean = false): String = lExec("pwd", quiet = quiet)!!.trim()

fun lExists(file: String): Boolean {
    val file = File(file)
    return file.exists()
}


// --------------------------------------------------------------------------------------------------------------
fun minutesSinceEpoch(): Int {
    //val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm")
    val epochDateTimeSeconds = LocalDateTime.parse("2020-01-01T00:00:00.000").toEpochSecond(ZoneOffset.UTC)
    //val sss = epochDate.toEpochSecond(ZoneOffset.UTC)
    val nowSeconds = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

    val diffMinutes = (nowSeconds - epochDateTimeSeconds) / 60
    return diffMinutes.toInt()
}