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
package se.skoview.lib

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.w3c.xhr.XMLHttpRequest
import pl.treksoft.kvision.core.Color
import pl.treksoft.kvision.rest.RestClient
import se.skoview.data.BaseItem
import kotlin.browser.window
import kotlin.js.Date
import kotlin.math.absoluteValue


fun getAsync(url: String, callback: (String) -> Unit) {
    console.log("getAsync(): URL: $url")
    val xmlHttp = XMLHttpRequest()
    xmlHttp.open("GET", url)
    xmlHttp.onload = {
        if (xmlHttp.readyState == 4.toShort() && xmlHttp.status == 200.toShort()) {
            callback.invoke(xmlHttp.responseText)
        }
    }
    xmlHttp.send()
}


// todo: Evaluate the use of the KVision client CallAgent. See CallAgentExample.kt
fun getAsyncTpDb(url: String, callback: (String) -> Unit) {
    val currentProtocol = window.location.protocol
    val currentHost = window.location.host
    // tpdb is assumed to be on the 'qa.integrationer.tjansteplattform.se' server if we run in development or test environment
    val baseUrl = if (currentHost.contains("localhost") || currentHost.contains("www.hippokrates.se")) {
        "https://qa.integrationer.tjansteplattform.se/tpdb/tpdbapi.php/api/v1/"
    }
    else {
        "$currentProtocol//$currentHost/../tpdb/tpdbapi.php/api/v1/"
    }
    val fullUrl = baseUrl + url
    console.log("URL: $fullUrl")

    val xmlHttp = XMLHttpRequest()
    xmlHttp.open("GET", fullUrl)
    xmlHttp.onload = {
        if (xmlHttp.readyState == 4.toShort() && xmlHttp.status == 200.toShort()) {
            callback.invoke(xmlHttp.responseText)
        }
    }
    xmlHttp.send()
}

fun getSyncTpDb(url: String): String? {
    val baseUrl = "https://qa.integrationer.tjansteplattform.se/tpdb/tpdbapi.php/api/v1/"
    val fullUrl = baseUrl + url
    //console.log("URL: $fullUrl")
    val xmlHttp = XMLHttpRequest()
    xmlHttp.open("GET", fullUrl, false)
    xmlHttp.send(null)

    return if (xmlHttp.status == 200.toShort()) {
        xmlHttp.responseText
    } else {
        null
    }
}

// Added an extension function to the Date class
fun Date.toSwedishDate(): String {

    val dd = this.getDate()
    val mm = this.getMonth() + 1 //January is 0!
    val yyyy = this.getFullYear()

    val sDD = if (dd > 9) dd.toString() else "0$dd"
    val sMM = if (mm > 9) mm.toString() else "0$mm"
    val sYYYY = yyyy.toString()

    return "$sYYYY-$sMM-$sDD"

    //return this.toISOString().substring(0, 10)
    //return this.toLocaleDateString().substring(0, 10)
}

fun getDatesLastMonth(): Pair<Date, Date> {

    val today = Date()
    println("Today: $today")
    val mm = today.getMonth() //January is 0!
    var yyyy = today.getFullYear()

    var month = mm
    if (month == 0) {
        month = 12
        yyyy -= 1
    }

    val firstDay = Date("${yyyy}-${month}-01")
    println("firstDay: $firstDay")
    println("firstDay.toISOString(): ${firstDay.toISOString()}")

    val lastDate = Date(yyyy, mm, 0)
    val lastDay = "$yyyy-$month-${lastDate.getDate()}"

    return Pair(firstDay, Date(lastDay))
}
/*
fun getColorForObject(obj: Any): Color {
    val cValue = obj.hashCode().absoluteValue
    val fValue = cValue.toDouble() / Int.MAX_VALUE.toDouble()
    val col = (fValue * 256 * 256 * 256 - 1).toInt()
    //return Color(col)
    return Color(col)
}
*/
