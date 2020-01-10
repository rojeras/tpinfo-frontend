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
    val integrationDates: List<Date>,
    val statisticsDates: List<Date>,
    val serviceComponents: Map<Int, ServiceComponent>,
    val logicalAddresses: Map<Int, LogicalAddress>,
    val serviceContracts: Map<Int, ServiceContract>,
    val serviceDomains: Map<Int, ServiceDomain>,
    val plattforms: Map<Int, Plattform>,
    val plattformChains: Map<Int, PlattformChain>,

    // Filter parameters
    val dateEffective: Date,
    val dateEnd: Date,
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

    params += TYPE_PARAM[ItemType.DATE_EFFECTIVE] + this.dateEffective?.toSwedishDate()
    params += TYPE_PARAM[ItemType.DATE_END] + this.dateEnd?.toSwedishDate()

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
    Date(), // todo: Verify if this is a good default - really want empty value
    Date(),
    listOf(),
    listOf(),
    listOf(),
    listOf(),
    listOf(),
    listOf(),
    listOf(),
    MaxCounter(-1, -1, -1, -1, -1, -1),
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
    }
    println("<---- After hippoReducer, action=${action::class}, new state is")
    console.log(newState)

    return newState
}

val store = createReduxStore(
    ::hippoReducer,
    INITIAL_STATE
)
// ---------------------------------------------------------------------------------------------------------------------
/*
@Serializable
data class ServiceComponent(
    val id: Int,
    val hsaId: String,
    val description: String = "",
    val synonym: String? = null
) {
    init {
        map[id] = this
    }

    val name: String = hsaId
    //override val itemType = ItemType.COMPONENT
    var searchField = "$name $description"

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is ServiceComponent) return false
        return id == other.id
    }

    override fun toString(): String {
        return "ServiceComponent(id=$id, name=$name, description=$description)"
    }

    override fun hashCode(): Int {
        return id
    }

    companion object {
        val map = hashMapOf<Int, ServiceComponent>()

        var isLoaded = false
    }
}
*/

/*
fun downloadServiceComponents(): ActionCreator<dynamic, HippoState> {
    return { dispatch, _ ->
        val baseUrl = "https://qa.integrationer.tjansteplattform.se/tpdb/tpdbapi.php/api/v1/components"
        //val baseUrl = "https://pokeapi.co/api/v2/pokemon/"
        println("After url")
        dispatch(HippoAction.StartDownloadBaseItems)
        println("After dispatch")

        getAsyncTpDb("components") { response ->
            println("Size of response is: ${response.length}")
            val json = Json(JsonConfiguration.Stable)
            val serviceComponents: List<ServiceComponent> =
                json.parse(ServiceComponent.serializer().list, response)

            console.log(serviceComponents)

            isLoaded = true
            dispatch(HippoAction.DownloadOkBaseItems)
            dispatch(HippoAction.SetServiceComponentList(serviceComponents))
        }
    }
}

 */
