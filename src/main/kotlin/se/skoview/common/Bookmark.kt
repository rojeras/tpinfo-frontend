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

import se.skoview.stat.PreSelect
import kotlin.js.Date

/**
 * Bookmark definition and functions
 *
 * The bookmark is stored in the "filter" parameter in the URL.
 * Syntax: "letter integer"
 *
 *  S: dateEffective (start date)
 *  E: dateEnd
 *  c: Consumer id
 *  d: Domain id
 *  C: Contract id
 *  l: Logical address id
 *  p: Producer id
 *  F: First plattform id
 *  M: Middle plattform id (optional)
 *  L: Last plattform id
 *  H1: Show history
 *  Dx: Display item type x, where x:
 *      1: consumers
 *      2: producers
 *      3: contracts
 *      4: logical addresses
 *  Px: Select preview x, where x:
 *      0: none selected (null)
 *      x > 0: According to preview id
 *
 * All bookmark functions except HippoState.applyBookmark() is defined in this file. applyBookmark
 * is an extension function defined in Model.kt.
 */

data class BookmarkInformation(
    var dateEffective: String? = null,
    var dateEnd: String? = null,
    var selectedConsumers: List<Int> = listOf(),
    var selectedProducers: List<Int> = listOf(),
    var selectedLogicalAddresses: List<Int> = listOf(),
    var selectedContracts: List<Int> = listOf(),
    var selectedDomains: List<Int> = listOf(),
    var selectedPlattformChains: List<Int> = listOf(),
    var showItemTypes: List<ItemType> = listOf(),
    var preView: PreSelect? = null,
    var showTimeGraph: Boolean = false,
)

fun HippoState.createBookmarkString(): String {

    var bookmark = ""

    if (this.view == View.HIPPO) {
        if (!(
            this.updateDates.isNullOrEmpty() ||
                this.dateEffective.isNullOrEmpty()
            )
        ) {
            // Exclude dates if dateEnd == current date (first in updateDates list)
            if (this.dateEnd != this.updateDates[0]) {
                bookmark += "S" + date2DaysSinceEpoch(this.dateEffective)
                bookmark += "E" + date2DaysSinceEpoch(this.dateEnd!!)
            }
        }
    } else {
        bookmark += "S" + date2DaysSinceEpoch(this.statDateEffective)
        bookmark += "E" + date2DaysSinceEpoch(this.statDateEnd)
    }

    bookmark += if (this.selectedConsumersIds.isNotEmpty()) this.selectedConsumersIds.joinToString(
        prefix = "c",
        separator = "c"
    ) else ""
    bookmark += if (this.selectedDomainsIds.isNotEmpty()) this.selectedDomainsIds.joinToString(
        prefix = "d",
        separator = "d"
    ) else ""
    bookmark += if (this.selectedContractsIds.isNotEmpty()) this.selectedContractsIds.joinToString(
        prefix = "C",
        separator = "C"
    ) else ""
    bookmark += if (this.selectedLogicalAddressesIds.isNotEmpty()) this.selectedLogicalAddressesIds.joinToString(
        prefix = "l",
        separator = "l"
    ) else ""
    bookmark += if (this.selectedProducersIds.isNotEmpty()) this.selectedProducersIds.joinToString(
        prefix = "p",
        separator = "p"
    ) else ""

    // Separate plattforms now stored in filter, not the chain
    console.log(this)
    for (pcId in this.selectedPlattformChainsIds) {
        val firstId = PlattformChain.mapp[pcId]?.first
        val lastId = PlattformChain.mapp[pcId]?.last
        bookmark += "F$firstId"
        bookmark += "L$lastId"
    }

    // History flag
    if (this.showTimeGraph) bookmark += "H1"

    // Add the showItemType settings
    if (this.showConsumers) bookmark += "D1"
    if (this.showProducers) bookmark += "D2"
    if (this.showContracts) bookmark += "D3"
    if (this.showLogicalAddresses) bookmark += "D4"

    bookmark += if (this.viewPreSelect == null) "P0"
    else "P${this.viewPreSelect.id}"

    return bookmark
}

// Let the URL mirror the current state
/*
fun setUrlFilter(state: HippoState) {
    val bookmark = state.getBookmark()
    val hostname = window.location.hostname
    val protocol = window.location.protocol
    val port = window.location.port
    val pathname = window.location.pathname

    val portSpec = if (port.isNotEmpty()) ":$port" else ""
    var newUrl = "$protocol//$hostname$portSpec$pathname"

    if (bookmark.length > 1) {
        newUrl += "?filter=$bookmark"
    }
    window.history.pushState(newUrl, "hippo-utforska integrationer", newUrl)
}
 */

fun parseBookmarkString(fullUrl: String?): BookmarkInformation {
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

    fun getFilterValue(fullUrl: String?): String? {
        if (fullUrl == null) return null

        val regex = Regex("""filter=[a-zA-Z0-9]*""")
        val matchResult: MatchResult? = regex.find(fullUrl)
        if (matchResult != null) {
            val fullFilter = matchResult.value
            if (fullFilter.startsWith("filter=")) return fullFilter.substring("filter=".length)
        }
        return null
    }
    // ---------------------------------------------------------------------

    println("In parseBookmarkString, string=$fullUrl")

    val filterValue: String? = getFilterValue(fullUrl)

    if (filterValue == null) return BookmarkInformation(
        // We end up here at initial start up when application is invoked without filter
        preView = PreSelect.getDefault(),
        showItemTypes = listOf(ItemType.CONSUMER)
    )

    /*
    val filterParam = "filter"
    var ix = fullUrl.indexOf(filterParam)

    if (ix < 0) return BookmarkInformation(
        // We end up here at initial start up when application is invoked without filter
        preView = PreSelect.getDefault(),
        showItemTypes = listOf(ItemType.CONSUMER)
    )

    ix += filterParam.length + 1

    val filterValueStart = fullUrl.substring(ix)
    val parts = filterValueStart.split('&')
    val filterValue = parts[0]
     */
    println("filterValue='$filterValue'")

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

    val showTimeGraphCodeList = parseBookmarkType("H", filterValue)
    val showTimeGraphFlag: Boolean =
        if (showTimeGraphCodeList.size == 0) false
        else showTimeGraphCodeList[0] == 1

    val showItemTypesList = mutableListOf<ItemType>()
    val showItemTypesCodeList = parseBookmarkType("D", filterValue)

    if (showItemTypesCodeList.contains(1)) showItemTypesList.add(ItemType.CONSUMER)
    if (showItemTypesCodeList.contains(2)) showItemTypesList.add(ItemType.PRODUCER)
    if (showItemTypesCodeList.contains(3)) showItemTypesList.add(ItemType.CONTRACT)
    if (showItemTypesCodeList.contains(4)) showItemTypesList.add(ItemType.LOGICAL_ADDRESS)

    val preViewId: Int
    val preViewodeList = parseBookmarkType("P", filterValue)

    if (preViewodeList.isEmpty()) preViewId = 0
    else preViewId = preViewodeList[0]

    val preView: PreSelect? =
        if (filterValue.isEmpty()) PreSelect.getDefault()
        else if (preViewId == 0) null
        else PreSelect.idMapp[preViewId]

    return BookmarkInformation(
        dateEffective,
        dateEnd,
        parseBookmarkType("c", filterValue),
        parseBookmarkType("p", filterValue),
        parseBookmarkType("l", filterValue),
        parseBookmarkType("C", filterValue),
        parseBookmarkType("d", filterValue),
        plattformChainList,
        showItemTypesList,
        preView,
        showTimeGraphFlag
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
