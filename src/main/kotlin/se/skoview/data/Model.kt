package se.skoview.data

import org.w3c.dom.get
import pl.treksoft.kvision.redux.createReduxStore
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Date

enum class SyncActionStatus {
    NOT_DONE,
    DONE,
    ERROR
}

enum class AsyncActionStatus {
    NOT_INITIALIZED,
    INITIALIZED,
    COMPLETED,
    ERROR
}


//@Serializable
data class HippoState(
    // Status information
    val currentAction: HippoAction,
    val applicationStarted: Boolean,
    val downloadBaseItemStatus: AsyncActionStatus,
    val downloadIntegrationStatus: AsyncActionStatus,
    val recreateViewData: Boolean = false, // Default is false
    val errorMessage: String?,

    // Base Items
    val integrationDates: List<String>,
    val statisticsDates: List<String>,
    val serviceComponents: Map<Int, ServiceComponent>,
    val logicalAddresses: Map<Int, LogicalAddress>,
    val serviceContracts: Map<Int, ServiceContract>,
    val serviceDomains: Map<Int, ServiceDomain>,
    val plattforms: Map<Int, Plattform>,
    val plattformChains: Map<Int, PlattformChain>,

    // Filter parameters
    val dateEffective: String,
    val dateEnd: String,

    val selectedConsumers: List<Int>,
    val selectedProducers: List<Int>,
    val selectedLogicalAddresses: List<Int>,
    val selectedContracts: List<Int>,
    val selectedDomains: List<Int>,
    val selectedPlattformChains: List<Int>,

    // Integrations data
    val integrationArrs: List<Integration>,
    val maxCounters: MaxCounter,
    val updateDates: List<String>,

    // View data
    val vServiceConsumers: List<ServiceComponent>,
    val vServiceProducers: List<ServiceComponent>,
    val vServiceDomains: List<ServiceDomain>,
    val vServiceContracts: List<ServiceContract>,
    val vDomainsAndContracts: List<BaseItem>,
    val vPlattformChains: List<PlattformChain>,
    val vLogicalAddresses: List<LogicalAddress>,

    // Text search filter
    val consumerFilter: String,
    val producerFilter: String,
    val contractFilter: String,
    val domainFilter: String,
    val logicalAddressFilter: String,
    val plattformChainFilter: String
)

// The extension function create the part of the URL to fetch integrations
fun HippoState.getParams(): String {

    //var params = "?dummy&contractId=379"
    var params = "?dummy"

    params += if (this.selectedConsumers.isNotEmpty()) this.selectedConsumers.joinToString(
        prefix = "&consumerId=",
        separator = ","
    ) else ""
    params += if (this.selectedDomains.isNotEmpty()) this.selectedDomains.joinToString(
        prefix = "&domainId=",
        separator = ","
    ) else ""
    params += if (this.selectedContracts.isNotEmpty()) this.selectedContracts.joinToString(
        prefix = "&contractId=",
        separator = ","
    ) else ""
    params += if (this.selectedLogicalAddresses.isNotEmpty()) this.selectedLogicalAddresses.joinToString(
        prefix = "&logicalAddressId=",
        separator = ","
    ) else ""
    params += if (this.selectedProducers.isNotEmpty()) this.selectedProducers.joinToString(
        prefix = "&producerId=",
        separator = ","
    ) else ""

    params += "&dateEffective=" + this.dateEffective
    params += "&dateEnd=" + this.dateEnd

    // Separate plattforms now stored in filter, not the chain
    for (pcId in this.selectedPlattformChains) {
        val firstId = PlattformChain.map[pcId]?.first
        val lastId = PlattformChain.map[pcId]?.last
        params += "&firstPlattformId=" + firstId
        params += "&lastPlattformId=" + lastId
    }

    return params
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
    val dateEffective = if (dateEffectiveCodeList.size > 0)  daysSinceEpoch2date(dateEffectiveCodeList[0].toInt()) else ""

    val dateEndCodeList = parseBookmarkType("E", filterValue)
    val dateEnd = if (dateEndCodeList.size > 0)  daysSinceEpoch2date(dateEffectiveCodeList[0].toInt()) else ""

    println("dates are : $dateEffective, $dateEnd")
    // Extract and calculate the plattforms values
    val firstPlattformCodeList = parseBookmarkType("F", filterValue)
    val lastPlattformCodeList = parseBookmarkType("L", filterValue)

    var plattformChainList = listOf<Int>()

    if (firstPlattformCodeList.size > 0 && lastPlattformCodeList.size > 0) {
        val first = firstPlattformCodeList[0]
        val last = lastPlattformCodeList[0]
        console.log("first=$first, last=$last")
        val plattformChainId = PlattformChain.calculateId(f = first, m = null, l = last)

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
    var date = Date((daysSinceEpoch + 16874) * 8.64e7)
    return date.toISOString().substring(0, 10)
}

fun HippoState.isItemFiltered(itemType: ItemType, id: Int): Boolean {
    return when (itemType) {
        ItemType.CONSUMER -> this.selectedConsumers.contains(id)
        ItemType.DOMAIN -> this.selectedDomains.contains(id)
        ItemType.CONTRACT -> this.selectedContracts.contains(id)
        ItemType.PLATTFORM_CHAIN -> this.selectedPlattformChains.contains(id)
        ItemType.LOGICAL_ADDRESS -> this.selectedLogicalAddresses.contains(id)
        ItemType.PRODUCER -> this.selectedProducers.contains(id)
        else -> {
            println("*** ERROR, unexpected type in isItemFiltered: $itemType")
            false
        }
    }
}

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

// This function creates the initial state based on an option filter parameter in the URL
fun getInitialState(): HippoState {

    val bookmarkInformation = parseBookmark()

    val initialState = HippoState(
        HippoAction.ApplicationStarted,
        false,
        AsyncActionStatus.NOT_INITIALIZED,
        AsyncActionStatus.NOT_INITIALIZED,
        false,
        null,
        listOf(),
        listOf(),
        mapOf(),
        mapOf(),
        mapOf(),
        mapOf(),
        mapOf(),
        mapOf(),
        bookmarkInformation.dateEffective, // todo: Verify if this is a good default - really want empty value
        bookmarkInformation.dateEnd,
        bookmarkInformation.selectedConsumers,
        bookmarkInformation.selectedProducers,
        bookmarkInformation.selectedLogicalAddresses,
        bookmarkInformation.selectedContracts,
        bookmarkInformation.selectedDomains,
        bookmarkInformation.selectedPlattformChains,
        listOf(),
        MaxCounter(0, 0, 0, 0, 0, 0),
        listOf(),
        listOf(),
        listOf(),
        listOf(),
        listOf(),
        listOf(),
        listOf(),
        listOf(),
        "",
        "",
        "",
        "",
        "",
        ""
    )
    return initialState
}

val store = createReduxStore(
    ::hippoReducer,
    getInitialState()
)

