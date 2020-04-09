/**
 * Copyright (C) 2013-2020 Lars Erik Röjerås
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package se.skoview.data

fun hippoReducer(state: HippoState, action: HippoAction): HippoState {
    //println("=====>>> ${action::class}")
    //console.log(state)
    val newState = when (action) {
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
        /*
        is HippoAction.StartDownloadIntegrations -> {
            state.copy(
                downloadIntegrationStatus = AsyncActionStatus.INITIALIZED
            )
        }
         */
        is HippoAction.DoneDownloadIntegrations -> {
            val dates: MutableList<String> = mutableListOf()
            // Must ensure the selected date is part of the list of all dates
            // Otherwise the date selector might be empty
            dates.addAll(action.updateDates)
            dates.add(state.dateEffective)
            state.copy(
                downloadIntegrationStatus = AsyncActionStatus.COMPLETED,
                integrationArrs = action.integrationArrs,
                maxCounters = action.maxCounters,
                updateDates = dates.distinct().sortedDescending(), //action.updateDates
                vServiceConsumersMax = 100,
                vServiceProducersMax = 100,
                vLogicalAddressesMax = 100
            )
        }
        is HippoAction.DoneDownloadStatistics -> {
            state.copy(
                callsConsumer = action.callsConsumer,
                callsProducer = action.callsProducer,
                callsLogicalAddress = action.callsLogicalAddress,
                callsDomain = action.callsDomain,
                callsContract = action.callsContract
            )
        }
        is HippoAction.ErrorDownloadIntegrations -> state.copy(
            downloadIntegrationStatus = AsyncActionStatus.ERROR,
            errorMessage = action.errorMessage
        )
        is HippoAction.DateSelected -> {
            when (action.dateType) {
                DateType.EFFECTIVE -> state.copy(dateEffective = action.selectedDate)
                DateType.END -> state.copy(dateEnd = action.selectedDate)
                DateType.EFFECTIVE_AND_END -> state.copy(
                    dateEffective = action.selectedDate,
                    dateEnd = action.selectedDate
                )
            }
        }
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

            val newList = if (state.isItemSelected(itemType = action.viewType, id = id)) listOf() else listOf(id)

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
        is HippoAction.SetVMax -> {
            when (action.type) {
                ItemType.CONSUMER -> state.copy(
                    vServiceConsumersMax = action.size
                )
                ItemType.LOGICAL_ADDRESS -> state.copy(
                    vLogicalAddressesMax = action.size
                )
                ItemType.PRODUCER -> state.copy(
                    vServiceProducersMax = action.size
                )
                else -> {
                    println("Internal error i the ShowMoreItems reducer")
                    state
                }
            }
        }
    }
    val finalState = newState.copy(currentAction = action::class)

    //console.log(newState)
    println("<<<===== ${action::class}")
    console.log(finalState)

    return finalState
}

