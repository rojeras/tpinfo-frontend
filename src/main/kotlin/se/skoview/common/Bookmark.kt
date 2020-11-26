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
package se.skoview.common

import kotlin.js.Date

data class BookmarkInformation(
    var dateEffective: String? = null,
    var dateEnd: String? = null,
    var selectedConsumers: List<Int> = listOf(),
    var selectedProducers: List<Int> = listOf(),
    var selectedLogicalAddresses: List<Int> = listOf(),
    var selectedContracts: List<Int> = listOf(),
    var selectedDomains: List<Int> = listOf(),
    var selectedPlattformChains: List<Int> = listOf()
)

fun HippoState.createBookmarkString(): String {
    println("getBookmars(), updateDates:")
    println("Length ${this.updateDates.size}")

    var bookmark = ""

    if (this.view == View.HIPPO) {
        if (this.updateDates.isNullOrEmpty() || this.dateEffective.isNullOrEmpty()) return ""

        // Exclude dates if dateEnd == current date (first in updateDates list)
        if (this.dateEnd != this.updateDates[0]) {
            bookmark += "S" + date2DaysSinceEpoch(this.dateEffective)
            bookmark += "E" + date2DaysSinceEpoch(this.dateEnd!!)
        }
    } else {
        bookmark += "S" + date2DaysSinceEpoch(this.statDateEffective)
        bookmark += "E" + date2DaysSinceEpoch(this.statDateEnd)
    }

    bookmark += if (this.selectedConsumers.isNotEmpty()) this.selectedConsumers.joinToString(
        prefix = "c",
        separator = "c"
    ) else ""
    bookmark += if (this.selectedDomains.isNotEmpty()) this.selectedDomains.joinToString(
        prefix = "d",
        separator = "d"
    ) else ""
    bookmark += if (this.selectedContracts.isNotEmpty()) this.selectedContracts.joinToString(
        prefix = "C",
        separator = "C"
    ) else ""
    bookmark += if (this.selectedLogicalAddresses.isNotEmpty()) this.selectedLogicalAddresses.joinToString(
        prefix = "l",
        separator = "l"
    ) else ""
    bookmark += if (this.selectedProducers.isNotEmpty()) this.selectedProducers.joinToString(
        prefix = "p",
        separator = "p"
    ) else ""

    // Separate plattforms now stored in filter, not the chain
    for (pcId in this.selectedPlattformChains) {
        println("In getBookmark()")
        val firstId = PlattformChain.map[pcId]?.first
        val lastId = PlattformChain.map[pcId]?.last
        bookmark += "F$firstId"
        bookmark += "L$lastId"
    }

    return bookmark
}

fun parseBookmarkString(fullUrl: String): BookmarkInformation {
    // ---------------------------------------------------------------------
    fun parseBookmarkType(typeChar: String, filterValue: String): List<Int> {
        // val regex = Regex("""c\d*""")
        val regexPattern = """\d*"""
        val regex = Regex(typeChar + regexPattern)

        val idWithCharList = regex.findAll(filterValue).toList()

        val iDList = mutableListOf<Int>()
        for (idWithCar in idWithCharList) {
            val id = idWithCar.value.drop(1).toInt()
            iDList.add(id)
        }
        return iDList
    }
    // ---------------------------------------------------------------------

    println("In parseBookmarkString, string=$fullUrl")

    val filterParam = "filter"
    var ix = fullUrl.indexOf(filterParam)
    if (ix < 0) return BookmarkInformation()

    ix += filterParam.length + 1

    val filterValueStart = fullUrl.substring(ix)
    val parts = filterValueStart.split('&')
    val filterValue = parts[0]

    // Extract and calculate the date values for hippo
    val dateEffectiveCodeList = parseBookmarkType("S", filterValue)
    val dateEffective = if (dateEffectiveCodeList.isNotEmpty())
        daysSinceEpoch2date(dateEffectiveCodeList[0])
    else null

    val dateEndCodeList = parseBookmarkType("E", filterValue)
    val dateEnd = if (dateEndCodeList.isNotEmpty())
        daysSinceEpoch2date(dateEndCodeList[0])
    else null

    // Extract and calculate the plattforms values
    val firstPlattformCodeList = parseBookmarkType("F", filterValue)
    val lastPlattformCodeList = parseBookmarkType("L", filterValue)

    var plattformChainList = listOf<Int>()

    if (firstPlattformCodeList.isNotEmpty() && lastPlattformCodeList.isNotEmpty()) {
        val first = firstPlattformCodeList[0]
        val last = lastPlattformCodeList[0]
        val plattformChainId = PlattformChain.calculateId(first = first, middle = null, last = last)

        plattformChainList = listOf(plattformChainId)
    }

    return BookmarkInformation(
        dateEffective,
        dateEnd,
        parseBookmarkType("c", filterValue),
        parseBookmarkType("p", filterValue),
        parseBookmarkType("l", filterValue),
        parseBookmarkType("C", filterValue),
        parseBookmarkType("d", filterValue),
        plattformChainList
    )
}

private fun date2DaysSinceEpoch(dateString: String): Double {
    val day = Date(dateString)
    return (day.getTime() / 8.64e7) - 16874 // Dived by number of millisecs since epoch (1/1 1970)
}

private fun daysSinceEpoch2date(daysSinceEpoch: Int): String {
    val date = Date((daysSinceEpoch + 16874) * 8.64e7)
    return date.toISOString().substring(0, 10)
}
