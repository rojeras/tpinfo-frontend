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
package se.skoview.model

import se.skoview.controller.View
import se.skoview.controller.getDatesLastMonth
import se.skoview.controller.toSwedishDate
import se.skoview.view.hippo.ItemType
import se.skoview.view.stat.PreSelect
import kotlin.js.Date

/**
 * Class which is used to codify the state.
 * The bookmark object is used when state is translated to and from the filter URL parameter (bookmark string)
 *
 *
 * @param dateEffective Start date of a selection
 * @param dateEnd: End date of a selection
 * @param selectedConsumers List of selected items of this type
 * @param selectedProducers List of selected items of this type
 * @param selectedLogicalAddresses List of selected items of this type
 * @param selectedContracts List of selected items of this type
 * @param selectedDomains List of selected items of this type
 * @param selectedConsumers List of selected item of this type
 * @param selectedPlattformChains List of selected item of this type
 * @param showItemTypes Specify which item types should be displayed (currently used in statistics only)
 * @param showTimeGraph Display history graph in statistics if set to true
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

/**
 * Extension function to the redux store.
 * Reads information from the store and creates the bookmark string (the part after "filter=" in the URL).
 * @return String which codifies the state to be appended to the URL
 *
 * The syntax of the codified state information in the resulting string is a letter followed by an integer id.
 * The following letters are used:
 *
 *  * S: dateEffective (start date)
 *  * E: dateEnd
 *  * c: Consumer id
 *  * d: Domain id
 *  * C: Contract id
 *  * l: Logical address id
 *  * p: Producer id
 *  * F: First plattform id
 *  * M: Middle plattform id (optional)
 *  * L: Last plattform id
 *  * H1: Show history
 *  * Dx: Display item type x, where x:
 *      * 1: consumers
 *      * 2: producers
 *      * 3: contracts
 *      * 4: logical addresses
 *  * Px: Select preview x, where x:
 *      * 0: none selected (null)
 *      * x > 0: According to preview id
 */
fun HippoState.createBookmarkString(): String {

    var bookmark = ""

    if (this.view == View.HIPPO) {
        // Exclude dates if dateEnd == current date (first in updateDates list)
        if (!(this.updateDates.isNullOrEmpty() || this.dateEffective.isNullOrEmpty()) && this.dateEnd != this.updateDates[0]) {
            bookmark += "S" + date2DaysSinceEpoch(this.dateEffective)
            bookmark += "E" + date2DaysSinceEpoch(this.dateEnd!!)
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

    // Statistic specific flags
    if (this.view == View.STAT) {
        // History flag
        if (this.showTimeGraph) bookmark += "H1"

        // Add the showItemType settings
        if (this.showConsumers) bookmark += "D1"
        if (this.showProducers) bookmark += "D2"
        if (this.showContracts) bookmark += "D3"
        if (this.showLogicalAddresses) bookmark += "D4"

        bookmark += if (this.viewPreSelect == null) "P0"
        else "P${this.viewPreSelect.id}"
    }

    return bookmark
}

/**
 * Extension function to the redux state.
 * The state is updated based on a bookmark object.
 * @param view View that should be displayed
 * @param bookmark an object with state information
 * @return An updated state
 */
fun HippoState.applyBookmark(view: View, bookmark: BookmarkInformation): HippoState {

    val newState =

        if (view == View.HIPPO) {
            val newDateEffective: String? =
                if (bookmark.dateEffective != null) bookmark.dateEffective
                else this.dateEffective
            val newDateEnd: String? =
                if (bookmark.dateEnd != null) bookmark.dateEnd
                else this.dateEnd

            this.copy(
                dateEffective = newDateEffective,
                dateEnd = newDateEnd,
            )
        } else {
            val datesLastMonth = getDatesLastMonth()

            val newDateEffective: String =
                if (bookmark.dateEffective != null) bookmark.dateEffective!!
                else datesLastMonth.first.toSwedishDate()

            val newDateEnd: String =
                if (bookmark.dateEnd != null) bookmark.dateEnd!!
                else datesLastMonth.second.toSwedishDate()

            var showConsumers: Boolean
            var showProducers: Boolean
            var showContracts: Boolean
            var showLogicalAddresses: Boolean

            if (bookmark.showItemTypes.isNotEmpty()) {
                showConsumers = if (bookmark.showItemTypes.contains(ItemType.CONSUMER)) true else false
                showProducers = if (bookmark.showItemTypes.contains(ItemType.PRODUCER)) true else false
                showContracts = if (bookmark.showItemTypes.contains(ItemType.CONTRACT)) true else false
                showLogicalAddresses =
                    if (bookmark.showItemTypes.contains(ItemType.LOGICAL_ADDRESS)) true else false
            } else {
                // If no display flag is set the default is to show all item types
                showConsumers = true
                showProducers = true
                showContracts = true
                showLogicalAddresses = true
            }

            val showTimeGraph = bookmark.showTimeGraph
            if (showTimeGraph) {
                showConsumers = true
                showProducers = true
                showContracts = true
                showLogicalAddresses = true
            }

            this.copy(
                statDateEffective = newDateEffective,
                statDateEnd = newDateEnd,
                showConsumers = showConsumers,
                showProducers = showProducers,
                showContracts = showContracts,
                showLogicalAddresses = showLogicalAddresses,
                showTimeGraph = showTimeGraph
            )
        }

    val nextState = newState.copy(
        view = view,
        selectedConsumersIds = bookmark.selectedConsumers,
        selectedProducersIds = bookmark.selectedProducers,
        selectedLogicalAddressesIds = bookmark.selectedLogicalAddresses,
        selectedContractsIds = bookmark.selectedContracts,
        selectedDomainsIds = bookmark.selectedDomains,
        selectedPlattformChainsIds = bookmark.selectedPlattformChains,
        viewPreSelect = bookmark.preView
    )

    return nextState
}

/**
 * Creates a bookmark object based on a bookmark string
 * @param fullUrl An URL string which contains a "filter=" parameter which is parsed
 * @return A bookmark object based on the input string
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
        if (!fullUrl.contains("filter=")) return null

        val regex = Regex("""filter=[a-zA-Z0-9]*""") // Will find last "filter=" string if more than one
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
    println("filterValue='$filterValue'")

    if (filterValue == null)
        return BookmarkInformation(
            // We end up here at initial start up when application is invoked without filter
            preView = PreSelect.getDefault(),
            showItemTypes = listOf(ItemType.CONSUMER)
        )

    // Extract and calculate the date values for hippo
    val dateEffectiveCodeList = parseBookmarkType("S", filterValue)
    val dateEffective = if (dateEffectiveCodeList.isNotEmpty())
        daysSinceEpoch2date(dateEffectiveCodeList[0])
    else BaseDates.getLastIntegrationDate()

    val dateEndCodeList = parseBookmarkType("E", filterValue)
    val dateEnd = if (dateEndCodeList.isNotEmpty())
        daysSinceEpoch2date(dateEndCodeList[0])
    else BaseDates.getLastIntegrationDate()

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

/**
 * Support function to convert a date to an integer value to be used in a bookmakr string
 * @param   dateString Date in string format
 * @return The number of days between 1/1 1970 and dateString
 */
private fun date2DaysSinceEpoch(dateString: String): Double {
    val day = Date(dateString)
    return (day.getTime() / 8.64e7) - 16874 // Dived by number of millisecs since epoch (1/1 1970)
}

/**
 * Support function to convert an integer representing days since 1/1 1970 to a date in string format
 * @param daysSinceEpoch Number of days since 1/1 1970
 * @return Date
 */
private fun daysSinceEpoch2date(daysSinceEpoch: Int): String {
    val date = Date((daysSinceEpoch + 16874) * 8.64e7)
    return date.toISOString().substring(0, 10)
}
