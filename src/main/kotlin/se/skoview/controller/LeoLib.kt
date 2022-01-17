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
package se.skoview.controller

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.xhr.XMLHttpRequest
import kotlin.js.Date

/**
 * Return the first part of the URL to TPDB-api with support for different development environments.
 */
fun tpdbBaseUrl(): String {
    val currentProtocol = window.location.protocol
    val currentHost = window.location.host
    // tpdb is assumed to be on the 'qa.integrationer.tjansteplattform.se' server if we run in development or test environment
    return if (
        currentHost.contains( "localhost" ) ||
        currentHost.contains("192.168.0.") ||
        currentHost.contains("www.hippokrates.se")
    ) {
        "https://qa.integrationer.tjansteplattform.se/tpdb/tpdbapi.php/api/v1/"
    } else {
        "$currentProtocol//$currentHost/../tpdb/tpdbapi.php/api/v1/"
    }
}

// todo: Evaluate the use of the KVision client CallAgent. See CallAgentExample.kt
/**
 * Make an asynchronous call to TPDB and invoke callback on response.
 *
 * @param url API specific part of the URL (excluding host information)
 * @param callback lambda to invoke on the response
 */
fun getAsyncTpDb(url: String, callback: (String) -> Unit) {

    val fullUrl = tpdbBaseUrl() + url
    console.log("URL: $fullUrl")

    val xmlHttp = XMLHttpRequest()
    // xmlHttp.onload = {
    xmlHttp.onreadystatechange = {
        if (xmlHttp.readyState == 4.toShort() && xmlHttp.status == 200.toShort()) {
            callback.invoke(xmlHttp.responseText)
        }
    }
    xmlHttp.open("GET", fullUrl, true)
    xmlHttp.send()
}

/**
 * Extension function to the Date class to convert a date to ISO representation string.
 *
 * @return YYYY-MM-DD formatted string
 */
fun Date.toSwedishDate(): String {

    val dd = this.getDate()
    val mm = this.getMonth() + 1 // January is 0!
    val yyyy = this.getFullYear()

    val sDD = if (dd > 9) dd.toString() else "0$dd"
    val sMM = if (mm > 9) mm.toString() else "0$mm"
    val sYYYY = yyyy.toString()

    return "$sYYYY-$sMM-$sDD"
}

/**
 * Returns two dates representing first and last day of last month.
 *
 * @return Pair of first and last [Date] of last month.
 */
fun getDatesLastMonth(): Pair<Date, Date> {

    val today = Date()
    println("Today: $today")
    val mm = today.getMonth() // January is 0!
    var yyyy = today.getFullYear()

    var month = mm
    if (month == 0) {
        month = 12
        yyyy -= 1
    }

    val firstDay = Date("$yyyy-$month-01")

    val lastDate = Date(yyyy, mm, 0)
    val lastDay = "$yyyy-$month-${lastDate.getDate()}"

    return Pair(firstDay, Date(lastDay))
}

/**
 * Obtains the version information. It is added to a meta tag with id=hippoVersion,
 * as part of the [build process](bin/buildDockerImage.kts).
 *
 * @return Version string
 */
fun getVersion(versionName: String = "hippoVersion"): String {
    val versionElement = document.getElementById(versionName)

    return if (versionElement != null) versionElement.getAttribute("content") ?: "-1.-1.-1"
    else "-2.-2.-2"
}

/**
 * String extension function that adds a space between every three character. Intended to format big numbers
 * whith space as thousands separator. "123456789" becomes "123 456 789"
 * @return The string where a space is inserted betwwen every third character,
 */
fun String.thousands(): String {
    val s1 = this.reversed()
    val s2List = s1.chunked(3)
    var s3 = ""

    for (item in s2List) {
        s3 += "$item "
    }

    return s3.trim().reversed()
}


