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
    val selectedConsumerIds: List<Int>,
    val selectedProducerIds: List<Int>,
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

    var params = "?dummy&contractId=379"
    //var params = "?dummy"
/*
    for ((key, _) in activeFilter) {
        if ((key != ItemType.PLATTFORM_CHAIN) && (activeFilter[key]!!.size > 0)) params += activeFilter[key]!!
            .sorted()
            .joinToString(prefix = typeParam[key]!!, separator = ",")
    }

    // Separate plattforms now stored in filter, not the chain
    for (pcId in activeFilter[ItemType.PLATTFORM_CHAIN]!!) {
        val firstId = PlattformChain.map[pcId]?.first
        val lastId = PlattformChain.map[pcId]?.last
        params += typeParam[ItemType.FIRST_PLATTFORM] + firstId
        params += typeParam[ItemType.LAST_PLATTFORM] + lastId
    }
*/

    params += TYPE_PARAM[ItemType.DATE_EFFECTIVE] + this.dateEffective
    params += TYPE_PARAM[ItemType.DATE_END] + this.dateEnd

    return params
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
}

fun hippoReducer(state: HippoState, action: HippoAction): HippoState {
    println("----> In hippoReducer, action=${action::class}, current state is")
    console.log(state)

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
            dateEnd = action.selectedDate)
    }
    println("<---- After hippoReducer, action=${action::class}, new state is")
    console.log(newState)

    return newState
}

val store = createReduxStore(
    ::hippoReducer,
    INITIAL_STATE
)

