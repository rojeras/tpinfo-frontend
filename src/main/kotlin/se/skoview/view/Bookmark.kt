package se.skoview.view

import se.skoview.data.HippoState
import se.skoview.data.PlattformChain
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Date

data class BookmarkInformation(
    var dateEffective: String = "",
    var dateEnd: String = "",

    var selectedConsumers: List<Int> = listOf(),
    var selectedProducers: List<Int> = listOf(),
    var selectedLogicalAddresses: List<Int> = listOf(),
    var selectedContracts: List<Int> = listOf(),
    var selectedDomains: List<Int> = listOf(),
    var selectedPlattformChains: List<Int> = listOf()
)

// Let the URL mirror the current state
fun setUrlFilter(state: HippoState) {
    val bookmark = state.getBookmark()
    println("In serUrlFilter(), bookmark=$bookmark")
    val hostname = window.location.hostname;
    val protocol = window.location.protocol;
    val port = window.location.port;

    val portSpec = if (port.length > 0) ":$port" else ""
    var newUrl = protocol + "//" + hostname + portSpec

    if (bookmark.length > 1) {
        newUrl += "?filter=" + bookmark
    }
    console.log("New URL: " + newUrl)
    window.history.pushState(newUrl, "hippo-utforska integrationer", newUrl)
}

fun HippoState.getBookmark(): String {
    if (this.updateDates.size == 0 || this.dateEffective == "") return ""

    var bookmark = ""

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

    // Exclude dates if dateEnd == current date (first in updateDates list)

    if (this.dateEnd != this.updateDates[0]) {
        bookmark += "S" + date2DaysSinceEpoch(this.dateEffective)
        bookmark += "E" + date2DaysSinceEpoch(this.dateEnd)
    }

    // Separate plattforms now stored in filter, not the chain
    for (pcId in this.selectedPlattformChains) {
        val firstId = PlattformChain.map[pcId]?.first
        val lastId = PlattformChain.map[pcId]?.last
        bookmark += "F" + firstId
        bookmark += "L" + lastId
    }

    println("Bookmark is: $bookmark")
    return bookmark

}

fun parseBookmark(): BookmarkInformation {
    // ---------------------------------------------------------------------
    fun parseBookmarkType(typeChar: String, filterValue: String): List<Int> {
        //val regex = Regex("""c\d*""")
        val regexPattern = """\d*"""
        val regex = Regex(typeChar + regexPattern)

        val idWithCharList = regex.findAll(filterValue).toList()

        var iDList = mutableListOf<Int>()
        for (idWithCar in idWithCharList) {
            val id = idWithCar.value.drop(1).toInt()
            iDList.add(id)
        }
        return iDList
    }
    // ---------------------------------------------------------------------

    val fullUrl = document.baseURI

    val filterParam = "filter"
    var ix = fullUrl.indexOf(filterParam)
    if (ix < 0) return BookmarkInformation()

    ix += filterParam.length + 1

    val filterValueStart = fullUrl.substring(ix)
    val parts = filterValueStart.split('&')
    val filterValue = parts[0]
    println("bookmark: $filterValue")

    // Extract and calculate the date values
    val dateEffectiveCodeList = parseBookmarkType("S", filterValue)
    val dateEffective =
        if (dateEffectiveCodeList.size > 0) daysSinceEpoch2date(dateEffectiveCodeList[0].toInt()) else ""

    val dateEndCodeList = parseBookmarkType("E", filterValue)
    val dateEnd = if (dateEndCodeList.size > 0) daysSinceEpoch2date(dateEffectiveCodeList[0].toInt()) else ""

    println("dates are : $dateEffective, $dateEnd")
    // Extract and calculate the plattforms values
    val firstPlattformCodeList = parseBookmarkType("F", filterValue)
    val lastPlattformCodeList = parseBookmarkType("L", filterValue)

    var plattformChainList = listOf<Int>()

    if (firstPlattformCodeList.size > 0 && lastPlattformCodeList.size > 0) {
        val first = firstPlattformCodeList[0]
        val last = lastPlattformCodeList[0]
        console.log("first=$first, last=$last")
        val plattformChainId = PlattformChain.calculateId(first = first, middle = null, last = last)

        plattformChainList = listOf(plattformChainId)
    }

    val bookmarkInformation = BookmarkInformation(
        dateEffective,
        dateEnd,
        parseBookmarkType("c", filterValue),
        parseBookmarkType("p", filterValue),
        parseBookmarkType("l", filterValue),
        parseBookmarkType("C", filterValue),
        parseBookmarkType("d", filterValue),
        plattformChainList
    )
    //val cList = parseBookmarkType("c", filterValue)
    println("Final bookmarkInformation:")
    console.log(bookmarkInformation)

    return bookmarkInformation
}

fun date2DaysSinceEpoch(dateString: String): Double {
    val day = Date(dateString)

    return (day.getTime() / 8.64e7) - 16874  // Dived by number of millisecs since epoch (1/1 1970)
}

fun daysSinceEpoch2date(daysSinceEpoch: Int): String {
    val date = Date((daysSinceEpoch + 16874) * 8.64e7)
    return date.toISOString().substring(0, 10)
}
