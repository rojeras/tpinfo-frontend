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

import se.skoview.stat.AdvancedViewPreSelect
import se.skoview.stat.StatisticsBlob
import se.skoview.stat.itemsFilter

fun hippoReducer(state: HippoState, action: HippoAction): HippoState {
    println("=====Reducer>>> ${action::class} : action = ")
    console.log(action)

    val newState = when (action) {

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
                downloadBaseDatesStatus = AsyncActionStatus.COMPLETED,
                /*
                integrationDates = BaseDates.integrationDates,
                statisticsDates = BaseDates.statisticsDates,
                serviceComponents = ServiceComponent.map,
                logicalAddresses = LogicalAddress.map,
                serviceContracts = ServiceContract.map,
                serviceDomains = ServiceDomain.map,
                plattforms = Plattform.mapp,
                plattformChains = PlattformChain.map,
                statisticsPlattforms = StatisticsPlattform.mapp,
                 */
                dateEffective = BaseDates.integrationDates[0],
                dateEnd = BaseDates.integrationDates[0],
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
            state.applyBookmark(action.view, action.bookmark)
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
            state.copy(historyMap = action.historyMap)
        }

        is HippoAction.ErrorDownloadIntegrations -> state.copy(
            downloadIntegrationStatus = AsyncActionStatus.ERROR,
            errorMessage = action.errorMessage
        )

        is HippoAction.DateSelected -> {
            state.dateSelected(action.selectedDate, action.dateType)
        }

        is HippoAction.StatTpSelected -> {
            val newState: HippoState =
                // todo: Remove hard coded tp id below
                if (action.tpId != 3) {
                    applyFilteredItemsSelection(state, AdvancedViewPreSelect.getDefault()!!.itemsFilter)
                        .copy(advancedViewPreSelect = AdvancedViewPreSelect.getDefault())
                } else state

            newState.statTpSelected(action.tpId)
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
            state.setNewView(action.view)
        }

        is HippoAction.ShowConsumers -> { state.copy(showConsumers = action.isShown) }
        is HippoAction.ShowProduceras -> { state.copy(showProducers = action.isShown) }
        is HippoAction.ShowLogicalAddresses -> { state.copy(showLogicalAddresses = action.isShown) }
        is HippoAction.ShowContracts -> { state.copy(showContracts = action.isShown) }
        is HippoAction.ShowTechnicalTerms -> { state.copy(showTechnicalTerms = action.isShown) }

        is HippoAction.SetSimpleViewPreselect -> {
            val preSelect = action.preSelect
            applyFilteredItemsSelection(state, preSelect.itemsFilter).copy(simpleViewPreSelect = preSelect)
        }

        is HippoAction.SetAdvancedViewPreselect -> {
            val preSelect = action.preSelect
            applyFilteredItemsSelection(state, preSelect.itemsFilter).copy(advancedViewPreSelect = preSelect)
        }
    }

    val finalState = newState.copy(currentAction = action::class)

    console.log(finalState)
    println("<<<===== ${action::class}")

    return finalState
}

private fun itemIdListSelected(inState: HippoState, itemType: ItemType, selectedList: List<Int>): HippoState {
    return when (itemType) {
        ItemType.CONSUMER -> inState.copy(selectedConsumersIds = selectedList)
        ItemType.CONTRACT -> inState.copy(selectedContractsIds = selectedList)
        ItemType.LOGICAL_ADDRESS -> inState.copy(selectedLogicalAddressesIds = selectedList)
        ItemType.PRODUCER -> inState.copy(selectedProducersIds = selectedList)
        else -> inState
    }
}

private fun itemDeselectAllForAllTypes(inState: HippoState): HippoState {
    return inState.copy(
        selectedConsumersIds = listOf(),
        selectedDomainsIds = listOf(),
        selectedContractsIds = listOf(),
        // selectedPlattformChains = listOf(),
        selectedLogicalAddressesIds = listOf(),
        selectedProducersIds = listOf()
    )
}

fun applyFilteredItemsSelection(inState: HippoState, itemsFilter: itemsFilter): HippoState {

    println("In applyFilteredItemsSelection(): $itemsFilter")

    var state2 = itemDeselectAllForAllTypes(inState)

    for ((itemType, itemIdList) in itemsFilter.selectedItems) {
        state2 = itemIdListSelected(state2, itemType, itemIdList)
    }

    return state2
}
