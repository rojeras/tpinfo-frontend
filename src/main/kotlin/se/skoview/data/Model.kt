package se.skoview.data

import pl.treksoft.kvision.redux.RAction
import pl.treksoft.kvision.redux.createReduxStore
import se.skoview.lib.toSwedishDate
import kotlin.js.Date


//@Serializable
data class HippoState(
    // Status infomration
    val downloadingBaseItems: Boolean,
    val downloadingIntegrations: Boolean,
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

    // View data structures
    val vServiceConsumers: List<ServiceComponent>,
    val vServiceProducers: List<ServiceComponent>,
    val vServiceDomains: List<ServiceDomain>,
    val vServiceContracts: List<ServiceContract>,
    val vDomainsAndContracts: List<BaseItem>,
    val vPlattformChains: List<PlattformChain>,
    val vLogicalAddresses: List<LogicalAddress>

)

// The extension function create the part of the URL to fetch integrations
fun HippoState.getParams(): String {

    //var params = "?dummy&contractId=379"
    var params = "?dummy"

    params += if (this.selectedConsumers.isNotEmpty()) this.selectedConsumers.joinToString(prefix = "&consumerId=", separator = ",") else ""
    params += if (this.selectedDomains.isNotEmpty()) this.selectedDomains.joinToString(prefix = "&domainId=", separator = ",") else ""
    params += if (this.selectedContracts.isNotEmpty()) this.selectedContracts.joinToString(prefix = "&contractId=", separator = ",") else ""
    params += if (this.selectedLogicalAddresses.isNotEmpty()) this.selectedLogicalAddresses.joinToString(prefix = "&logicalAddressId=", separator = ",") else ""
    params += if (this.selectedProducers.isNotEmpty()) this.selectedProducers.joinToString(prefix = "&producerId=", separator = ",") else ""

    params += TYPE_PARAM[ItemType.DATE_EFFECTIVE] + this.dateEffective
    params += TYPE_PARAM[ItemType.DATE_END] + this.dateEnd

    // Separate plattforms now stored in filter, not the chain
    for (pcId in this.selectedPlattformChains) {
        val firstId = PlattformChain.map[pcId]?.first
        val lastId = PlattformChain.map[pcId]?.last
        params += "&firstPlattformId=" + firstId
        params += "&lastPlattformId=" + lastId
    }

    return params
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

val INITIAL_STATE = HippoState(
    false,
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
    "", // todo: Verify if this is a good default - really want empty value
    "",
    listOf(),
    listOf(),
    listOf(),
    listOf(),
    listOf(),
    listOf(),
    listOf(),
    MaxCounter(0, 0, 0, 0, 0, 0),
    listOf(),
    listOf(),
    listOf(),
    listOf(),
    listOf(),
    listOf(),
    listOf(),
    listOf()
)

sealed class HippoAction : RAction {
    object StartDownloadBaseItems : HippoAction()
    object DoneDownloadBaseItems : HippoAction()
    object StartDownloadIntegrations : HippoAction()
    data class DoneDownloadIntegrations(
        val integrationArrs: List<Integration>,
        val maxCounters: MaxCounter,
        val updateDates: List<String>
    ) : HippoAction()

    data class DownloadErrorBaseItems(val errorMessage: String) : HippoAction()
    data class ViewUpdated(
        val vServiceConsumers: List<ServiceComponent>,
        val vServiceProducers: List<ServiceComponent>,
        val vServiceDomains: List<ServiceDomain>,
        val vServiceContracts: List<ServiceContract>,
        val vDomainsAndContracts: List<BaseItem>,
        val vPlattformChains: List<PlattformChain>,
        val vLogicalAddresses: List<LogicalAddress>
        ) : HippoAction()
    data class DateSelected(val selectedDate: String) : HippoAction()
    data class ItemSelected(
        val viewType: ItemType,
        val baseItem: BaseItem
    ) : HippoAction()
}

fun hippoReducer(state: HippoState, action: HippoAction): HippoState {
    println("----> In hippoReducer, action=${action::class}")
    //console.log(state)

    val newState = when (action) {
        is HippoAction.StartDownloadBaseItems -> state.copy(downloadingBaseItems = true)
        is HippoAction.DoneDownloadBaseItems -> {
            state.copy(
                downloadingBaseItems = false,
                integrationDates = BaseDates.integrationDates,
                statisticsDates = BaseDates.statisticsDates,
                serviceComponents = ServiceComponent.map,
                logicalAddresses = LogicalAddress.map,
                serviceContracts = ServiceContract.map,
                serviceDomains = ServiceDomain.map,
                plattforms = Plattform.map,
                plattformChains = PlattformChain.map,

                dateEffective = BaseDates.integrationDates[0],
                dateEnd = BaseDates.integrationDates[0]
            )
        }
        is HippoAction.DownloadErrorBaseItems -> state.copy(
            downloadingBaseItems = false,
            errorMessage = action.errorMessage
        )
        is HippoAction.StartDownloadIntegrations -> {
            state.copy(downloadingIntegrations = true)
        }
        is HippoAction.DoneDownloadIntegrations -> state.copy(
            downloadingIntegrations = false,
            integrationArrs = action.integrationArrs,
            maxCounters = action.maxCounters,
            updateDates = action.updateDates
        )
        is HippoAction.ViewUpdated -> state.copy(
            vServiceConsumers = action.vServiceConsumers,
            vServiceProducers = action.vServiceProducers,
            vServiceDomains = action.vServiceDomains,
            vServiceContracts = action.vServiceContracts,
            vDomainsAndContracts = action.vDomainsAndContracts,
            vPlattformChains = action.vPlattformChains,
            vLogicalAddresses = action.vLogicalAddresses
        )
        is HippoAction.DateSelected -> state.copy(
            dateEffective = action.selectedDate,
            dateEnd = action.selectedDate
        )
        is HippoAction.ItemSelected -> {
            val id = action.baseItem.id

            val newList = if (state.isItemFiltered(itemType = action.viewType, id = id)) listOf() else listOf(id)

            when (action.viewType) {
                ItemType.CONSUMER -> state.copy( selectedConsumers = newList )
                ItemType.DOMAIN -> state.copy( selectedDomains = newList )
                ItemType.CONTRACT -> state.copy( selectedContracts = newList )
                ItemType.PLATTFORM_CHAIN -> state.copy( selectedPlattformChains = newList )
                ItemType.LOGICAL_ADDRESS -> state.copy( selectedLogicalAddresses = newList )
                ItemType.PRODUCER -> state.copy( selectedProducers = newList )
                else -> {
                    println("*** ERROR in when clause for reduare ItemSelected: ${action.viewType}")
                    state
                }
            }
        }
    }

    return newState
}

val store = createReduxStore(
    ::hippoReducer,
    INITIAL_STATE
)

