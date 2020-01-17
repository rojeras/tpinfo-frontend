package se.skoview.data

import pl.treksoft.kvision.redux.RAction

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
