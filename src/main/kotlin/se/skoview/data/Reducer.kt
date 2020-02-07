package se.skoview.data

import se.skoview.view.createViewData


fun hippoReducer(state: HippoState, action: HippoAction): HippoState {
    println("=====>>> ${action::class}")
    //console.log(state)
    val newState = when (action) {
        is HippoAction.FilterConsumers -> state.copy(
            currentAction = action,
            recreateViewData = true,
            consumerFilter = action.consumerFilter
        )
        is HippoAction.ApplicationStarted -> state.copy(
            currentAction = action,
            applicationStarted = true
        )
        is HippoAction.StartDownloadBaseItems -> state.copy(
            currentAction = action,
            downloadBaseItemStatus = AsyncActionStatus.INITIALIZED
        )
        is HippoAction.DoneDownloadBaseItems -> {
            state.copy(
                currentAction = action,
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
            currentAction = action,
            downloadBaseItemStatus = AsyncActionStatus.ERROR,
            errorMessage = action.errorMessage
        )
        is HippoAction.StartDownloadIntegrations -> {
            state.copy(
                currentAction = action,
                recreateViewData = false,
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
                currentAction = action,
                recreateViewData = true,
                downloadIntegrationStatus = AsyncActionStatus.COMPLETED,
                integrationArrs = action.integrationArrs,
                maxCounters = action.maxCounters,
                updateDates = dates.distinct().sortedDescending() //action.updateDates
            )
        }
        is HippoAction.ErrorDownloadIntegrations -> state.copy(
            currentAction = action,
            downloadIntegrationStatus = AsyncActionStatus.ERROR,
            errorMessage = action.errorMessage
        )
        is HippoAction.DateSelected -> state.copy(
            currentAction = action,
            recreateViewData = false,
            dateEffective = action.selectedDate,
            dateEnd = action.selectedDate
        )
        is HippoAction.ViewUpdated -> state.copy(
            currentAction = action,
            recreateViewData = false,
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
                    currentAction = action,
                    recreateViewData = true,
                    selectedConsumers = newList
                )
                ItemType.DOMAIN -> state.copy(
                    currentAction = action,
                    recreateViewData = true,
                    selectedDomains = newList
                )
                ItemType.CONTRACT -> state.copy(
                    currentAction = action,
                    recreateViewData = true,
                    selectedContracts = newList
                )
                ItemType.PLATTFORM_CHAIN -> state.copy(
                    currentAction = action,
                    recreateViewData = true,
                    selectedPlattformChains = newList
                )
                ItemType.LOGICAL_ADDRESS -> state.copy(
                    currentAction = action,
                    recreateViewData = true,
                    selectedLogicalAddresses = newList
                )
                ItemType.PRODUCER -> state.copy(
                    currentAction = action,
                    recreateViewData = true,
                    selectedProducers = newList
                )
                else -> {
                    println("*** ERROR in when clause for reducer ItemSelected: ${action.viewType}")
                    state
                }
            }
        }
    }

    console.log(newState)
    println("<<<===== ${action::class}")

    return newState
}

// Called after each state change (reducer)
fun stateChangeTrigger(state: HippoState) {
    println("+++++>>> ${state.currentAction::class}")
    when (state.currentAction) {
        // Load base items at application start
        is HippoAction.ApplicationStarted -> {
            println(">> stateChangeTrigger() - when currentState == ApplicationStarted")
            loadBaseItems(store)
        }
        is HippoAction.DoneDownloadBaseItems -> loadIntegrations(state)

        is HippoAction.DoneDownloadIntegrations -> createViewData(state)
        //is HippoAction.ItemSelected -> loadIntegrations(state)
        is HippoAction.ItemSelected -> createViewData(state)
        is HippoAction.FilterConsumers -> createViewData(state)
    }

    println("<<<+++++ ${state.currentAction::class}")
}
