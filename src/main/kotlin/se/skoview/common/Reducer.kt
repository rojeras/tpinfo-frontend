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
import se.skoview.stat.FilteredItems
import se.skoview.stat.StatisticsBlob

fun hippoReducer(state: HippoState, action: HippoAction): HippoState {
    println("=====Reducer>>> ${action::class} : action = ")
    console.log(action)

    val newState = when (action) {
        is HippoAction.ApplicationStarted -> {
            // If dates not set by URL at startup the default are set here
            val dEffective: String
            val dEnd: String
            val pChainId: List<Int>
            val app: HippoApplication
            val view: View
            when (action.App) {
                HippoApplication.HIPPO -> {
                    dEffective = if (state.dateEffective == null) BaseDates.integrationDates[0] else state.dateEffective
                    dEnd = dEffective
                    pChainId = state.selectedPlattformChains
                    app = HippoApplication.HIPPO
                    view = View.HIPPO
                }
                HippoApplication.STATISTIK -> {
                    app = HippoApplication.STATISTIK
                    val datePair = getDatesLastMonth()
                    dEffective = datePair.first.toSwedishDate()
                    // todo: Following should not be hard coded like this
                    pChainId = listOf(calculateId(first = 3, middle = null, last = 3))
                    dEnd = datePair.second.toSwedishDate()
                    view = View.STAT_SIMPLE
                }
            }
            state.copy(
                applicationStarted = app,
                dateEffective = dEffective,
                selectedPlattformChains = pChainId,
                dateEnd = dEnd,
                view = view
            )
        }
        // is HippoAction.SetView -> state.copy(view = action.view)
        is HippoAction.SetDownloadBaseDatesStatus -> {
            if (action.status == AsyncActionStatus.COMPLETED)
                state.copy(
                    dateEffective = BaseDates.integrationDates[0],
                    dateEnd = BaseDates.integrationDates[0],
                    downloadBaseDatesStatus = action.status
                )
            else state.copy(downloadBaseDatesStatus = AsyncActionStatus.COMPLETED)
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
                statisticsPlattforms = StatisticsPlattform.mapp,
                // Try this
            )
        }
        is HippoAction.ErrorDownloadBaseItems -> state.copy(
            downloadBaseItemStatus = AsyncActionStatus.ERROR,
            errorMessage = action.errorMessage
        )

        is HippoAction.StartDownloadIntegrations -> state.copy(
            downloadIntegrationStatus = AsyncActionStatus.INITIALIZED
        )

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
        is HippoAction.ApplyBookmark -> {
            val newDateEffective: String? =
                if (action.bookmark.dateEffective != null) action.bookmark.dateEffective
                else state.dateEffective
            val newDateEnd: String? =
                if (action.bookmark.dateEnd != null) action.bookmark.dateEnd
                else state.dateEnd

            state.copy(
                view = action.view,
                dateEffective = newDateEffective,
                dateEnd = newDateEnd,
                selectedConsumers = action.bookmark.selectedConsumers,
                selectedProducers = action.bookmark.selectedProducers,
                selectedLogicalAddresses = action.bookmark.selectedLogicalAddresses,
                selectedContracts = action.bookmark.selectedContracts,
                selectedDomains = action.bookmark.selectedDomains,
                selectedPlattformChains = action.bookmark.selectedPlattformChains
            )
        }
        is HippoAction.StartDownloadStatistics ->
            state.copy(
                downloadStatisticsStatus = AsyncActionStatus.INITIALIZED
            )
        is HippoAction.DoneDownloadStatistics -> {
            val statBlob = StatisticsBlob(action.statisticsArrArr)
            state.copy(
                downloadStatisticsStatus = AsyncActionStatus.COMPLETED,
                statBlob = statBlob
            )
        }
        is HippoAction.StartDownloadHistory ->
            state.copy(
                downloadHistoryStatus = AsyncActionStatus.INITIALIZED
            )
        is HippoAction.DoneDownloadHistory -> {
            println("In reducer DonwDownloadHistory")
            state.copy(historyMap = action.historyMap)
        }
        is HippoAction.ErrorDownloadIntegrations -> state.copy(
            downloadIntegrationStatus = AsyncActionStatus.ERROR,
            errorMessage = action.errorMessage
        )
        is HippoAction.DateSelected -> {
            state.dateSelected(action.selectedDate, action.dateType)
            /*
            when (action.dateType) {
                DateType.EFFECTIVE -> state.copy(dateEffective = action.selectedDate)
                DateType.END -> state.copy(dateEnd = action.selectedDate)
                DateType.EFFECTIVE_AND_END -> state.copy(
                    dateEffective = action.selectedDate,
                    dateEnd = action.selectedDate
                )
                DateType.STAT_EFFECTIVE -> state.copy(statDateEffective = action.selectedDate)
                DateType.STAT_END -> state.copy(statDateEnd = action.selectedDate)
            }
            */
        }
        is HippoAction.StatTpSelected -> {
            state.statTpSelected(action.tpId)
        }
        is HippoAction.ItemIdSelected -> {
            state.itemIdSeclected(action.id, action.viewType)
        }
        is HippoAction.ItemIdDeselected -> {
            state.itemIdSeclected(action.id, action.viewType)
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

        is HippoAction.SetView -> {
            state.setView(action.view)
        }

        is HippoAction.ShowTechnicalTerms -> {
            state.copy(
                showTechnicalTerms = action.isShown
            )
        }

        is HippoAction.SetSimpleViewPreselect -> {
            println("In reducer SetSimpleViewPreSelect")
            val preSelect = action.preSelect
            applyFilteredItemsSelection(state, preSelect.filteredItems).copy(simpleViewPreSelect = preSelect)
        }

        is HippoAction.SetAdvancedViewPreselect -> {
            println("In reducer SetAdvancedViewPreSelect")
            val preSelect = action.preSelect
            applyFilteredItemsSelection(state, preSelect.filteredItems).copy(advancedViewPreSelect = preSelect)
        }
    }

    val finalState = newState.copy(currentAction = action::class)

    console.log(finalState)
    println("<<<===== ${action::class}")

    return finalState
}

private fun itemIdListSelected(inState: HippoState, itemType: ItemType, selectedList: List<Int>): HippoState {
    return when (itemType) {
        ItemType.CONSUMER -> inState.copy(selectedConsumers = selectedList)
        ItemType.CONTRACT -> inState.copy(selectedContracts = selectedList)
        ItemType.LOGICAL_ADDRESS -> inState.copy(selectedLogicalAddresses = selectedList)
        ItemType.PRODUCER -> inState.copy(selectedProducers = selectedList)
        else -> inState
    }
}

private fun itemDeselectAllForAllTypes(inState: HippoState): HippoState {
    return inState.copy(
        selectedConsumers = listOf(),
        selectedDomains = listOf(),
        selectedContracts = listOf(),
        // selectedPlattformChains = listOf(),
        selectedLogicalAddresses = listOf(),
        selectedProducers = listOf()
    )
}

fun applyFilteredItemsSelection(inState: HippoState, filteredItems: FilteredItems): HippoState {

    println("In applyFilteredItemsSelection(): $filteredItems")

    var state2 = itemDeselectAllForAllTypes(inState)

    for ((itemType, itemIdList) in filteredItems.selectedItems) {
        state2 = itemIdListSelected(state2, itemType, itemIdList)
    }

    return state2
}

// todo: Maybe add common reducer logic in private functions here
// Would make it possible to do a number of state updates within the same action

// todo: Maybe add HippoThunk creators here
