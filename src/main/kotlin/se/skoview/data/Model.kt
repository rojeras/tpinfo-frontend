package se.skoview.data

import se.skoview.view.parseBookmark

enum class AsyncActionStatus {
    NOT_INITIALIZED,
    INITIALIZED,
    COMPLETED,
    ERROR
}

//@Serializable
data class HippoState(
    // Status information
    val applicationStarted: Boolean,
    val downloadBaseItemStatus: AsyncActionStatus,
    val downloadIntegrationStatus: AsyncActionStatus,
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

fun HippoState.isItemFiltered(itemType: ItemType, id: Int): Boolean {
    return when (itemType) {
        ItemType.CONSUMER -> this.selectedConsumers.contains(id)
        ItemType.DOMAIN -> this.selectedDomains.contains(id)
        ItemType.CONTRACT -> this.selectedContracts.contains(id)
        ItemType.PLATTFORM_CHAIN -> this.selectedPlattformChains.contains(id)
        ItemType.LOGICAL_ADDRESS -> this.selectedLogicalAddresses.contains(id)
        ItemType.PRODUCER -> this.selectedProducers.contains(id)
    }
}

// This function creates the initial state based on an option filter parameter in the URL
fun getInitialState(): HippoState {

    val bookmarkInformation = parseBookmark()

    return HippoState(
        false,
        AsyncActionStatus.NOT_INITIALIZED,
        AsyncActionStatus.NOT_INITIALIZED,
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
}


