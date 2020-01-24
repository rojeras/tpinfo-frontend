package se.skoview.data


fun hippoReducer(state: HippoState, action: HippoAction): HippoState {
    println("----> In hippoReducer, action=${action::class}")
    //console.log(state)

    val newState = when (action) {
        is HippoAction.ApplicationStarted -> state.copy(applicationStarted = true)
        is HippoAction.StartDownloadBaseItems -> state.copy(downloadBaseItemStatus = AsyncActionStatus.INITIALIZED)
        is HippoAction.DoneDownloadBaseItems -> {
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

                dateEffective = BaseDates.integrationDates[0],
                dateEnd = BaseDates.integrationDates[0]
            )
        }
        is HippoAction.ErrorDownloadBaseItems -> state.copy(
            downloadBaseItemStatus = AsyncActionStatus.ERROR,
            errorMessage = action.errorMessage
        )
        is HippoAction.StartDownloadIntegrations -> {
            state.copy(downloadIntegrationStatus = AsyncActionStatus.INITIALIZED)
        }
        is HippoAction.DoneDownloadIntegrations -> {
            var dates: MutableList<String> = mutableListOf()
            // Must ensure the selected date is part of the list of all dates
            // Otherwise the date selector might be empty
            dates.addAll(action.updateDates)
            dates.add(state.dateEffective)
            state.copy(
                downloadIntegrationStatus = AsyncActionStatus.COMPLETED,
                integrationArrs = action.integrationArrs,
                maxCounters = action.maxCounters,
                updateDates = dates.distinct().sortedDescending() //action.updateDates
            )

        }
        is HippoAction.ErrorDownloadIntegrations -> state.copy(
            downloadIntegrationStatus = AsyncActionStatus.ERROR,
            errorMessage = action.errorMessage
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
                ItemType.CONSUMER -> state.copy(selectedConsumers = newList)
                ItemType.DOMAIN -> state.copy(selectedDomains = newList)
                ItemType.CONTRACT -> state.copy(selectedContracts = newList)
                ItemType.PLATTFORM_CHAIN -> state.copy(selectedPlattformChains = newList)
                ItemType.LOGICAL_ADDRESS -> state.copy(selectedLogicalAddresses = newList)
                ItemType.PRODUCER -> state.copy(selectedProducers = newList)
                else -> {
                    println("*** ERROR in when clause for reducer ItemSelected: ${action.viewType}")
                    state
                }
            }
        }
    }

    return newState
}

// Called after each state change (reducer)
fun stateChangeTrigger(state: HippoState) {
    // Load base items at application start
    if (state.applicationStarted == true && state.downloadBaseItemStatus == AsyncActionStatus.NOT_INITIALIZED) {
        loadBaseItems(store)
        return
    }

    // Load integrations
    if ( state.downloadBaseItemStatus == AsyncActionStatus.COMPLETED
        && state.downloadIntegrationStatus == AsyncActionStatus.NOT_INITIALIZED ) {
        loadIntegrations(state)
        return
    }
}
