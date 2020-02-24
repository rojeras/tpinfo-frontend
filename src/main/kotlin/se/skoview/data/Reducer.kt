package se.skoview.data

fun hippoReducer(state: HippoState, action: HippoAction): HippoState {
    println("=====>>> ${action::class}")
    //console.log(state)
    val newState = when (action) {
        is HippoAction.FilterItems -> {
            when (action.type) {
                ItemType.CONSUMER -> state.copy(consumerFilter = action.filterString)
                ItemType.CONTRACT -> state.copy(contractFilter = action.filterString)
                ItemType.LOGICAL_ADDRESS -> state.copy(logicalAddressFilter = action.filterString)
                ItemType.PRODUCER -> state.copy(producerFilter = action.filterString)
                ItemType.PLATTFORM_CHAIN -> state.copy(plattformChainFilter = action.filterString)
                else -> {
                    println("Internal error i the filter reducer")
                    state
                }
            }
        }
        is HippoAction.ApplicationStarted -> state.copy(
            applicationStarted = true
        )
        is HippoAction.StartDownloadBaseItems -> state.copy(
            downloadBaseItemStatus = AsyncActionStatus.INITIALIZED
        )
        is HippoAction.DoneDownloadBaseItems -> {
            // If dates not set by URL at startup the default to latest date
            val newDate = if (state.dateEffective == "") BaseDates.integrationDates[0] else state.dateEffective

            state.copy(
                downloadBaseItemStatus = AsyncActionStatus.COMPLETED,
                integrationDates = BaseDates.integrationDates,
                statisticsDates = BaseDates.statisticsDates,
                serviceComponents = ServiceComponent.map,
                logicalAddresses = LogicalAddress.map,
                serviceContracts = ServiceContract.map,
                serviceDomains = ServiceDomain.map,
                plattforms = Plattform.map,
                plattformChains = PlattformChain.map,

                dateEffective = newDate,
                dateEnd = newDate
            )
        }
        is HippoAction.ErrorDownloadBaseItems -> state.copy(
            downloadBaseItemStatus = AsyncActionStatus.ERROR,
            errorMessage = action.errorMessage
        )
        is HippoAction.StartDownloadIntegrations -> {
            state.copy(
                downloadIntegrationStatus = AsyncActionStatus.INITIALIZED
            )
        }
        is HippoAction.DoneDownloadIntegrations -> {
            val dates: MutableList<String> = mutableListOf()
            // Must ensure the selected date is part of the list of all dates
            // Otherwise the date selector might be empty
            dates.addAll(action.updateDates)
            dates.add(state.dateEffective)
            state.copy(
                downloadIntegrationStatus = AsyncActionStatus.COMPLETED,
                integrationArrs = action.integrationArrs,
                activeIntegrationArrs = action.integrationArrs,
                maxCounters = action.maxCounters,
                updateDates = dates.distinct().sortedDescending() //action.updateDates
            )
        }
        is HippoAction.ErrorDownloadIntegrations -> state.copy(
            downloadIntegrationStatus = AsyncActionStatus.ERROR,
            errorMessage = action.errorMessage
        )
        is HippoAction.DateSelected -> state.copy(
            dateEffective = action.selectedDate,
            dateEnd = action.selectedDate
        )
        is HippoAction.ViewUpdated -> state.copy(
            vServiceConsumers = action.integrationLists.serviceConsumers,
            vServiceDomains = action.integrationLists.serviceDomains,
            vServiceContracts = action.integrationLists.serviceContracts,
            vDomainsAndContracts = action.integrationLists.domainsAndContracts,
            vPlattformChains = action.integrationLists.plattformChains,
            vLogicalAddresses = action.integrationLists.logicalAddresses,
            vServiceProducers = action.integrationLists.serviceProducers
        )
        is HippoAction.ItemSelected -> {

            val id = action.baseItem.id

            val newList = if (state.isItemFiltered(itemType = action.viewType, id = id)) listOf() else listOf(id)

            when (action.viewType) {
                ItemType.CONSUMER -> state.copy(
                    selectedConsumers = newList
                )
                ItemType.DOMAIN -> state.copy(
                    selectedDomains = newList
                )
                ItemType.CONTRACT -> state.copy(
                    selectedContracts = newList
                )
                ItemType.PLATTFORM_CHAIN -> state.copy(
                    selectedPlattformChains = newList
                )
                ItemType.LOGICAL_ADDRESS -> state.copy(
                    selectedLogicalAddresses = newList
                )
                ItemType.PRODUCER -> state.copy(
                    selectedProducers = newList
                )
            }
        }
    }

    console.log(newState)
    println("<<<===== ${action::class}")

    return newState
}

