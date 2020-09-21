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
package se.skoview.common

import se.skoview.common.PlattformChain.Companion.calculateId
import se.skoview.stat.StatPreSelect
import se.skoview.stat.StatisticsBlob

fun hippoReducer(state: HippoState, action: HippoAction): HippoState {
    //println("=====>>> ${action::class}")
    val newState = when (action) {
        is HippoAction.ApplicationStarted -> {
            // If dates not set by URL at startup the default are set here
            val dEffective: String
            val dEnd: String
            val pChainId: List<Int>
            val app: HippoApplication
            when (action.App) {
                HippoApplication.HIPPO -> {
                    dEffective = if (state.dateEffective == "") BaseDates.integrationDates[0] else state.dateEffective
                    dEnd = dEffective
                    pChainId = state.selectedPlattformChains
                    app = HippoApplication.HIPPO
                }
                HippoApplication.STATISTIK -> {
                    app = HippoApplication.STATISTIK
                    val datePair = getDatesLastMonth()
                    dEffective = datePair.first.toSwedishDate()
                    // todo: Following should not be hard coded like this
                    pChainId = listOf(calculateId(first = 3, middle = null, last = 3))
                    dEnd = datePair.second.toSwedishDate()
                }
            }
            state.copy(
                applicationStarted = app,
                dateEffective = dEffective,
                selectedPlattformChains = pChainId,
                dateEnd = dEnd
            )
        }
        is HippoAction.StartDownloadBaseItems -> state.copy(
            downloadBaseItemStatus = AsyncActionStatus.INITIALIZED
        )
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
                statisticsPlattforms = StatisticsPlattform.map
                //dateEffective = newDate,
                //dateEnd = newDate
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
            // Must ensure the selected date is part of the list of all dates
            // Otherwise the date selector might be empty
            state.copy(
                downloadIntegrationStatus = AsyncActionStatus.COMPLETED,
                integrationArrs = action.integrationArrs,
                maxCounters = action.maxCounters,
                updateDates = action.updateDates.toList(),
                vServiceConsumersMax = 100,
                vServiceProducersMax = 100,
                vLogicalAddressesMax = 100
            )
        }
        is HippoAction.DoneDownloadStatistics -> {
            val statBlob = StatisticsBlob(action.statisticsArrArr)
            state.copy(
                statBlob = statBlob
            )
        }
        is HippoAction.DoneDownloadHistory -> {
            println("In reducer DonwDownloadHistory")
            state.copy(historyMap = action.historyMap)
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

        is HippoAction.ItemIdSelected -> {

            val id = action.id

            when (action.viewType) {
                ItemType.CONSUMER -> {
                    val newList = listOf(state.selectedConsumers, listOf(id)).flatten().distinct()
                    state.copy(
                        selectedConsumers = newList
                    )
                }
                ItemType.DOMAIN -> {
                    val newList = listOf(state.selectedDomains, listOf(id)).flatten().distinct()
                    state.copy(
                        selectedDomains = newList
                    )
                }
                ItemType.CONTRACT -> {
                    val newList = listOf(state.selectedContracts, listOf(id)).flatten().distinct()
                    state.copy(
                        selectedContracts = newList
                    )
                }
                ItemType.PLATTFORM_CHAIN -> {
                    val newList = listOf(state.selectedPlattformChains, listOf(id)).flatten().distinct()
                    state.copy(
                        selectedPlattformChains = newList
                    )
                }
                ItemType.LOGICAL_ADDRESS -> {
                    val newList = listOf(state.selectedLogicalAddresses, listOf(id)).flatten().distinct()
                    state.copy(
                        selectedLogicalAddresses = newList
                    )
                }
                ItemType.PRODUCER -> {
                    val newList = listOf(state.selectedProducers, listOf(id)).flatten().distinct()
                    state.copy(
                        selectedProducers = newList
                    )
                }
            }
        }
        is HippoAction.ItemIdDeselected -> {

            val id = action.id

            when (action.viewType) {
                ItemType.CONSUMER -> {
                    val newList = state.selectedConsumers as MutableList<Int>
                    newList.remove(id)
                    state.copy(
                        selectedConsumers = newList
                    )
                }
                ItemType.DOMAIN -> {
                    val newList = state.selectedDomains as MutableList<Int>
                    newList.remove(id)
                    state.copy(
                        selectedDomains = newList
                    )
                }
                ItemType.CONTRACT -> {
                    val newList = state.selectedContracts as MutableList<Int>
                    newList.remove(id)
                    state.copy(
                        selectedContracts = newList
                    )
                }
                ItemType.PLATTFORM_CHAIN -> {
                    val newList = state.selectedPlattformChains as MutableList<Int>
                    newList.remove(id)
                    state.copy(
                        selectedPlattformChains = newList
                        //selectedPlattformName = PlattformChain.map[newList[0]]!!.name
                    )
                }
                ItemType.LOGICAL_ADDRESS -> {
                    val newList = state.selectedLogicalAddresses as MutableList<Int>
                    newList.remove(id)
                    state.copy(
                        selectedLogicalAddresses = newList
                    )
                }
                ItemType.PRODUCER -> {
                    val newList = state.selectedProducers as MutableList<Int>
                    newList.remove(id)
                    state.copy(
                        selectedProducers = newList
                    )
                }
            }
        }
        is HippoAction.ItemIdDeselectedAll -> {
            when (action.viewType) {
                ItemType.CONSUMER -> state.copy(selectedConsumers = listOf())
                ItemType.DOMAIN -> state.copy(selectedDomains = listOf())
                ItemType.CONTRACT -> state.copy(selectedContracts = listOf())
                ItemType.PLATTFORM_CHAIN -> state.copy(selectedPlattformChains = listOf())
                ItemType.LOGICAL_ADDRESS -> state.copy(selectedLogicalAddresses = listOf())
                ItemType.PRODUCER -> state.copy(selectedProducers = listOf())
            }
        }
        is HippoAction.ItemDeselectedAllForAllTypes -> {
            state.copy(
                selectedConsumers = listOf(),
                selectedDomains = listOf(),
                selectedContracts = listOf(),
                //selectedPlattformChains = listOf(),
                selectedLogicalAddresses = listOf(),
                selectedProducers = listOf()
            )
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
        is HippoAction.ShowTimeGraph -> {
            state.copy(showTimeGraph = action.isShown)
        }
        is HippoAction.StatAdvancedMode -> {
            state.copy(statAdvancedMode = action.isAdvanced)
        }
        is HippoAction.ShowTechnicalTerms -> {
            state.copy(
                showTechnicalTerms = action.isShown,
                consumerLabel = "Tjänstekonsumenter",
                contractLabel = "Tjänstekontrakt",
                producerLabel = "Tjänsteproducenter",
                laLabel = "Logiska adresser"
            )
        }
        is HippoAction.PreSelectedSelected -> {
            val preName = action.label
            val preObject = StatPreSelect.mapp[preName]
            val preLabelMap = preObject!!.labelMap

            state.copy(
                statPreSelect = action.label,
                consumerLabel = preLabelMap[ItemType.CONSUMER]!!,
                contractLabel = preLabelMap[ItemType.CONTRACT]!!,
                producerLabel = preLabelMap[ItemType.PRODUCER]!!,
                laLabel = preLabelMap[ItemType.LOGICAL_ADDRESS]!!
            )
        }
    }
    val finalState = newState.copy(currentAction = action::class)

    println("<<<===== ${action::class}")
    console.log(finalState)

    return finalState
}

// todo: Maybe add common reducer logic in private functions here
// Would make it possible to do a number of state updates within the same action

// todo: Maybe add HippoThunk creators here
