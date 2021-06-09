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
package se.skoview.model

import se.skoview.controller.View
import se.skoview.controller.getDatesLastMonth
import se.skoview.controller.toSwedishDate
import se.skoview.model.HippoAction.ShowTechnicalTerms
import se.skoview.model.HippoAction.ShowTimeGraph
import se.skoview.view.hippo.Integration
import se.skoview.view.hippo.ItemType
import se.skoview.view.hippo.MaxCounter
import se.skoview.view.stat.ItemsFilter
import se.skoview.view.stat.PreSelect
import se.skoview.view.stat.StatisticsBlob
import kotlin.reflect.KClass

/**
 * Defines the states an asynchronous call can take
 */
enum class AsyncActionStatus {
    NOT_INITIALIZED,
    INITIALIZED,
    COMPLETED,
    ERROR
}

/**
 * Defines the legal types for dates managed in the application.
 *
 * @param EFFECTIVE First date in a time span
 * @param END Last date in a time span
 * @param EFFECTIVE_AND_END Time span of one day
 * @param STAT_EFFECTIVE First date of a time span in Statistik
 * @param STAT_END End date of a time span in Statistik
 */
enum class DateType {
    EFFECTIVE,
    END,
    EFFECTIVE_AND_END,
    STAT_EFFECTIVE,
    STAT_END
}

/**
 * The redux state. The defaults serve as initial values.
 *
 * @param currentAction Used to store the last action (which created the current state).
 * @param view View (application) to present.
 * @param downloadBaseDatesStatus State of a certain asynchronous call.
 * @param downloadBaseItemStatus State of a certain asynchronous call.
 * @param downloadIntegrationStatus State of a certain asynchronous call.
 * @param downloadStatisticsStatus State of a certain asynchronous call.
 * @param downloadHistoryStatus State of a certain asynchronous call.
 * @param dateEffective The first date of a time span (used in hippo).
 * @param dateEnd The last date of a time span (used in hippo).
 * @param statDateEffective The first date in a time span (used in Statistik).
 * @param statDateEnd The last date in a time span (used in Statistik).
 * @param selectedConsumersIds List of id's (integers) representing currently selected consumers.
 * @param selectedProducersIds List of id's (integers) representing currently selected producers.
 * @param selectedLogicalAddressesIds List of id's (integers) representing currently selected logical addresses.
 * @param selectedContractsIds List of id's (integers) representing currently selected contracts.
 * @param selectedDomainsIds List of id's (integers) representing currently selected domains.
 * @param selectedPlattformChainsIds List of id's (integers) representing currently selected platform chains.
 * @param selectedPlattformName Name of currently selected TP (used in Statistik).
 * @param integrationArrs List of currently selected integrations.
 * @param maxCounters The number of items of each type that exist for the current time span (dateEffecitve to dateEnd). Shown in column headers in hippo.
 * @param updateDates The list of dates where the current selection changed. Used to populate the date list in hippo.
 * @param vServiceConsumersMax Maximal number of items to show/add of each type in the columns in hippo.
 * @param vServiceProducersMax Maximal number of items to show/add of each type in the columns in hippo.
 * @param vLogicalAddressesMax Maximal number of items to show/add of each type in the columns in hippo.
 * @param vServiceContractsMax Maximal number of items to show/add of each type in the columns in hippo.
 * @param statBlob Contains the statistic information for the current selection.
 * @param historyMap Contains the history data for the current selection.
 * @param showTimeGraph Flag to show/hide the history chart.
 * @param showTechnicalTerms Flag to show technical terms or their synonyms.
 * @param viewPreSelect Contains a selected [PreSelect]
 * @param showConsumers Flag to show/hide consumer chart in Statistik (not in use but kept for future extensions).
 * @param showProducers Flag to show/hide producer chart in Statistik (not in use but kept for future extensions).
 * @param showLogicalAddresses Flag to show/hide logical address chart in Statistik (not in use but kept for future extensions).
 * @param showContracts Flag to show/hide contract chart in Statistik (not in use but kept for future extensions).
 * @param lockShowAllItemTypes Override to ensure all type charts are displayed.
 */
data class HippoState(
    // Status information
    val currentAction: KClass<out HippoAction> = HippoAction.SetView::class,
    val view: View = View.HOME,
    val downloadBaseDatesStatus: AsyncActionStatus = AsyncActionStatus.NOT_INITIALIZED,
    val downloadBaseItemStatus: AsyncActionStatus = AsyncActionStatus.NOT_INITIALIZED,
    val downloadIntegrationStatus: AsyncActionStatus = AsyncActionStatus.NOT_INITIALIZED,
    val downloadStatisticsStatus: AsyncActionStatus = AsyncActionStatus.NOT_INITIALIZED,
    val downloadHistoryStatus: AsyncActionStatus = AsyncActionStatus.NOT_INITIALIZED,
    val errorMessage: String? = null,

    // Filter parameters
    val dateEffective: String? = null,
    val dateEnd: String? = null,
    val statDateEffective: String = "",
    val statDateEnd: String = "",

    val selectedConsumersIds: List<Int> = listOf(),
    val selectedProducersIds: List<Int> = listOf(),
    val selectedLogicalAddressesIds: List<Int> = listOf(),
    val selectedContractsIds: List<Int> = listOf(),
    val selectedDomainsIds: List<Int> = listOf(),
    val selectedPlattformChainsIds: List<Int> = listOf(),

    // Integrations data
    val selectedPlattformName: String = "",
    val integrationArrs: List<Integration> = listOf(),
    val maxCounters: MaxCounter = MaxCounter(0, 0, 0, 0, 0, 0),
    val updateDates: List<String> = listOf(),

    // Max number of items to display
    val vServiceConsumersMax: Int = 100,
    val vServiceProducersMax: Int = 100,
    val vLogicalAddressesMax: Int = 100,
    val vServiceContractsMax: Int = 500,

    // Statistics information
    val statBlob: StatisticsBlob = StatisticsBlob(arrayOf(arrayOf())),

    // History information
    val historyMap: Map<String, Int> = mapOf(),
    val showTimeGraph: Boolean = false,

    // View controllers
    val showTechnicalTerms: Boolean = false,
    val viewPreSelect: PreSelect? = null,
    val showConsumers: Boolean = true,
    val showProducers: Boolean = true,
    val showLogicalAddresses: Boolean = true,
    val showContracts: Boolean = true,
    val lockShowAllItemTypes: Boolean = false
)

/**
 * Initialize the state. Beside the default values the [HippoState.statDateEffective] and [HippoState.statDateEnd]
 * are set to start and end date of last month.
 */
fun initializeHippoState(): HippoState {
    val datesPair = getDatesLastMonth()
    val statDateEffective = datesPair.first.toSwedishDate()
    val statDateEnd = datesPair.second.toSwedishDate()

    return HippoState(
        statDateEffective = statDateEffective,
        statDateEnd = statDateEnd,
    )
}

/**
 * Extension function to the redux state. Counts how how many of the (statistik) views currently are visible.
 *
 * @return Number of visible views.
 */
fun HippoState.numberOfItemViewsSelected(): Int {
    var count = 0

    if (this.showConsumers) count++
    if (this.showProducers) count++
    if (this.showContracts) count++
    if (this.showLogicalAddresses) count++

    return count
}

/**
 * Extension function to the redux state. Checks if a certain item is selected.
 *
 * @param itemType Type of item to check.
 * @param id Id of the item to check.
 *
 * @return <i>true</i> if the item is currently selected.
 */
fun HippoState.isItemSelected(itemType: ItemType, id: Int): Boolean {
    return when (itemType) {
        ItemType.CONSUMER -> this.selectedConsumersIds.contains(id)
        ItemType.DOMAIN -> this.selectedDomainsIds.contains(id)
        ItemType.CONTRACT -> this.selectedContractsIds.contains(id)
        ItemType.PLATTFORM_CHAIN -> this.selectedPlattformChainsIds.contains(id)
        ItemType.LOGICAL_ADDRESS -> this.selectedLogicalAddressesIds.contains(id)
        ItemType.PRODUCER -> this.selectedProducersIds.contains(id)
    }
}

/**
 * Extension function to the redux state. Checks if a certain platform is selected in the statistik application.
 *
 * @return <i>true</i> if the plattform is currently selected.
 */
fun HippoState.isStatPlattformSelected(): Boolean {

    val plattformChains: List<Int> = this.selectedPlattformChainsIds
    val statisticsPlattforms: Map<Int, StatisticsPlattform> = StatisticsPlattform.mapp

    if (plattformChains.size != 1) return false
    val selectedPlattformChainId = plattformChains[0]
    val selectedPlattformChain = PlattformChain.mapp[selectedPlattformChainId]!!

    if (
        statisticsPlattforms.contains(selectedPlattformChain.first) ||
        statisticsPlattforms.contains(selectedPlattformChain.last)
    ) return true

    return false
}

/**
 * Extension function to the redux state. Based on state information the parameters for the TPDB GET calls is generated.
 *
 * @param view Indicate which application is active. The parameters for hippo and Statistik are slightly different.
 *
 * @return The query string part of the TPDB-API GET call.
 */
fun HippoState.getParams(view: View): String {

    var params = "?dummy"

    if (view == View.HIPPO) {
        params += "&dateEffective=" + this.dateEffective
        params += "&dateEnd=" + this.dateEnd
    } else {
        params += "&dateEffective=" + this.statDateEffective
        params += "&dateEnd=" + this.statDateEnd
    }

    params += if (this.selectedConsumersIds.isNotEmpty()) this.selectedConsumersIds.joinToString(
        prefix = "&consumerId=",
        separator = ","
    ) else ""
    params += if (this.selectedDomainsIds.isNotEmpty()) this.selectedDomainsIds.joinToString(
        prefix = "&domainId=",
        separator = ","
    ) else ""
    params += if (this.selectedContractsIds.isNotEmpty()) this.selectedContractsIds.joinToString(
        prefix = "&contractId=",
        separator = ","
    ) else ""
    params += if (this.selectedLogicalAddressesIds.isNotEmpty()) this.selectedLogicalAddressesIds.joinToString(
        prefix = "&logicalAddressId=",
        separator = ","
    ) else ""
    params += if (this.selectedProducersIds.isNotEmpty()) this.selectedProducersIds.joinToString(
        prefix = "&producerId=",
        separator = ","
    ) else ""

    // Separate plattforms now stored in filter, not the chain
    for (pcId in this.selectedPlattformChainsIds) {
        print("In getParams()")
        val firstId = PlattformChain.mapp[pcId]?.first
        val lastId = PlattformChain.mapp[pcId]?.last
        params += "&firstPlattformId=$firstId"
        params += "&lastPlattformId=$lastId"
    }

    return params
}

/**
 * Extension function to the redux state. Update the state with a flag value.
 *
 * @param action An action to set a flag in the state.
 * @return An updated state object.
 */
fun HippoState.setFlag(action: HippoAction): HippoState {
    return when (action) {
        is ShowTechnicalTerms -> this.copy(showTechnicalTerms = action.isShown)
        is ShowTimeGraph -> this.copy(showTimeGraph = action.isShown)
        else -> {
            println("Error in HippoState.setFlag(), action = $action")
            this
        }
    }
}

/**
 * Extension function to the redux state. Apply a pre-select specification to the state.
 *
 * @param preSelect The preSelect to apply.
 *
 * @return An updated state object.
 */
fun HippoState.setPreselect(preSelect: PreSelect?): HippoState {
    if (preSelect == null) {
        return this
            .setShowAllItemTypes(true)
            .itemDeselectAllForAllTypes()
            .copy(viewPreSelect = null)
    }

    val state2 = this.setShowAllItemTypes(false)
    val viewItemType: ItemType = preSelect.viewOrder[0]
    val state3 =
        when (viewItemType) {
            ItemType.CONSUMER -> state2.copy(showConsumers = true)
            ItemType.PRODUCER -> state2.copy(showProducers = true)
            ItemType.CONTRACT -> state2.copy(showContracts = true)
            ItemType.LOGICAL_ADDRESS -> state2.copy(showLogicalAddresses = true)
            else -> {
                println("Error in HippoAction.SetViewPreselect")
                state2
            }
        }
    return state3.applyFilteredItemsSelection(preSelect.itemsFilter).copy(viewPreSelect = preSelect)
}

/**
 * Extension function to the redux state. Make multiple selections for a type.
 *
 * @param itemType The type of the items to be selected
 * @param selectedList List of id's of [itemType] to be selected.
 *
 * @return An updated state object.
 */
fun HippoState.itemIdListSelected(itemType: ItemType, selectedList: List<Int>): HippoState {
    return when (itemType) {
        ItemType.CONSUMER -> this.copy(selectedConsumersIds = selectedList)
        ItemType.CONTRACT -> this.copy(selectedContractsIds = selectedList)
        ItemType.LOGICAL_ADDRESS -> this.copy(selectedLogicalAddressesIds = selectedList)
        ItemType.PRODUCER -> this.copy(selectedProducersIds = selectedList)
        else -> this
    }
}

/**
 * Extension function to the redux state. Clear all selections.
 *
 * @return Copy of the state where all selections have been cleared.
 */
fun HippoState.itemDeselectAllForAllTypes(): HippoState {
    return this.copy(
        selectedConsumersIds = listOf(),
        selectedDomainsIds = listOf(),
        selectedContractsIds = listOf(),
        // selectedPlattformChains = listOf(),
        selectedLogicalAddressesIds = listOf(),
        selectedProducersIds = listOf()
    )
}

/**
 * Extension function to the redux state.
 */
fun HippoState.applyFilteredItemsSelection(itemsFilter: ItemsFilter): HippoState {

    println("In applyFilteredItemsSelection(): $itemsFilter")

    var state2 = this.itemDeselectAllForAllTypes()

    for ((itemType, itemIdList) in itemsFilter.selectedItems) {
        state2 = state2.itemIdListSelected(itemType, itemIdList)
    }

    return state2
}

fun HippoState.itemIdSeclected(id: Int, type: ItemType): HippoState {

    return when (type) {
        ItemType.CONSUMER -> {
            val newList = listOf(this.selectedConsumersIds, listOf(id)).flatten().distinct()
            this.copy(
                selectedConsumersIds = newList
            )
        }
        ItemType.DOMAIN -> {
            val newList = listOf(this.selectedDomainsIds, listOf(id)).flatten().distinct()
            this.copy(
                selectedDomainsIds = newList
            )
        }
        ItemType.CONTRACT -> {
            val newList = listOf(this.selectedContractsIds, listOf(id)).flatten().distinct()
            this.copy(
                selectedContractsIds = newList
            )
        }
        ItemType.PLATTFORM_CHAIN -> {
            val newList = listOf(this.selectedPlattformChainsIds, listOf(id)).flatten().distinct()
            this.copy(
                selectedPlattformChainsIds = newList
            )
        }
        ItemType.LOGICAL_ADDRESS -> {
            val newList = listOf(this.selectedLogicalAddressesIds, listOf(id)).flatten().distinct()
            this.copy(
                selectedLogicalAddressesIds = newList
            )
        }
        ItemType.PRODUCER -> {
            val newList = listOf(this.selectedProducersIds, listOf(id)).flatten().distinct()
            this.copy(
                selectedProducersIds = newList
            )
        }
    }
}

fun HippoState.itemIdDeseclected(id: Int, type: ItemType): HippoState {

    // If the deselected item is part of the definition of the active preselect - then reset preselect
    val newPreSelect: PreSelect? = this.viewPreSelect

    if (this.view == View.STAT && this.viewPreSelect != null) {
        val preSelectedItems = this.viewPreSelect.itemsFilter.selectedItems
        if (preSelectedItems[type]!!.contains(id)) {
            return this
                .itemDeselectAllForAllTypes()
                .copy(
                    viewPreSelect = null
                )
        }
    }

    return when (type) {
        ItemType.CONSUMER -> {
            this.copy(
                selectedConsumersIds = this.selectedConsumersIds.filter { it != id },
                viewPreSelect = newPreSelect
            )
        }
        ItemType.DOMAIN -> {
            this.copy(
                selectedDomainsIds = this.selectedDomainsIds.filter { it != id },
                viewPreSelect = newPreSelect
            )
        }
        ItemType.CONTRACT -> {
            this.copy(
                selectedContractsIds = this.selectedContractsIds.filter { it != id },
                viewPreSelect = newPreSelect
            )
        }
        ItemType.PLATTFORM_CHAIN -> {
            this.copy(
                selectedPlattformChainsIds = this.selectedPlattformChainsIds.filter { it != id },
                viewPreSelect = newPreSelect
            )
        }
        ItemType.LOGICAL_ADDRESS -> {
            this.copy(
                selectedLogicalAddressesIds = this.selectedLogicalAddressesIds.filter { it != id },
                viewPreSelect = newPreSelect
            )
        }
        ItemType.PRODUCER -> {
            this.copy(
                selectedProducersIds = this.selectedProducersIds.filter { it != id },
                viewPreSelect = newPreSelect
            )
        }
    }
}

// is HippoAction.StatTpSelected -> {
fun HippoState.statTpSelected(tpId: Int): HippoState {

    val pChainId = PlattformChain.calculateId(first = tpId, middle = null, last = tpId)

    return this.itemDeselectAllForAllTypes().copy(
        selectedPlattformChainsIds = listOf(pChainId),
        viewPreSelect = null
        // viewPreSelect = preSelect,
    )
}

fun HippoState.setShowAllItemTypes(isShown: Boolean): HippoState {
    return this.copy(
        showConsumers = isShown,
        showProducers = isShown,
        showLogicalAddresses = isShown,
        showContracts = isShown,
    )
}

// is HippoAction.SetView -> {
fun HippoState.setNewView(newView: View): HippoState {

    val currentView = this.view
    if (currentView == newView) return this

    // Switch from hippo to statistics
    if (
        currentView == View.HIPPO &&
        (newView == View.STAT /* || newView == View.STAT_ADVANCED */)
    ) {
        if (this.selectedPlattformChainsIds.isNotEmpty()) {
            val pcId = this.selectedPlattformChainsIds[0]
            val pc = PlattformChain.mapp[pcId]!!
            val tpFirstId = Plattform.mapp[pc.first]!!.id
            val tpLastId = Plattform.mapp[pc.last]!!.id

            val tpId = if (StatisticsPlattform.mapp.containsKey(tpFirstId)) tpFirstId
            else if (StatisticsPlattform.mapp.containsKey(tpLastId)) tpLastId
            else Plattform.nameToId("SLL-PROD")

            return this.copy(
                selectedPlattformChainsIds = listOf(PlattformChain.calculateId(tpId!!, 0, tpId)),
                view = newView
            )
        }
    }

    // From statistics to hippo
    // Filter for all plattform chains containing a stat plattform
    if (
        (currentView == View.STAT /* || currentView == View.STAT_ADVANCED */) &&
        newView == View.HIPPO
    ) {
        return this.copy(
            // selectedPlattformChainsIds = StatisticsPlattform.getStatisticsPlattformChainIds(),
            selectedPlattformChainsIds = listOf(),
            view = newView
        )
    }

    return this.copy(view = newView)
}

fun HippoState.dateSelected(selectedDate: String, dateType: DateType): HippoState {
    //  is HippoAction.DateSelected -> {
    return when (dateType) {
        DateType.EFFECTIVE -> this.copy(dateEffective = selectedDate)
        DateType.END -> this.copy(dateEnd = selectedDate)
        DateType.EFFECTIVE_AND_END -> this.copy(
            dateEffective = selectedDate,
            dateEnd = selectedDate
        )
        DateType.STAT_EFFECTIVE -> this.copy(statDateEffective = selectedDate)
        DateType.STAT_END -> this.copy(statDateEnd = selectedDate)
    }
}

fun HippoState.isSelectedItemsUnChanged(oldState: HippoState): Boolean {
    return (
            this.selectedConsumersIds.equals(oldState.selectedConsumersIds) &&
                    this.selectedContractsIds.equals(oldState.selectedContractsIds) &&
                    this.selectedLogicalAddressesIds.equals(oldState.selectedLogicalAddressesIds) &&
                    this.selectedDomainsIds.equals(oldState.selectedDomainsIds) &&
                    this.selectedProducersIds.equals(oldState.selectedProducersIds) &&
                    this.selectedPlattformChainsIds.equals(oldState.selectedPlattformChainsIds)
            )
}

fun HippoState.isIntegrationSelectionsChanged(oldState: HippoState): Boolean {
    return !(
            this.dateEffective == oldState.dateEffective &&
                    this.dateEnd == oldState.dateEnd &&
                    this.isSelectedItemsUnChanged(oldState)
            )
}

fun HippoState.isStatisticsSelectionsChanged(oldState: HippoState): Boolean {
    return !(
            this.statDateEffective == oldState.statDateEffective &&
                    this.statDateEnd == oldState.statDateEnd &&
                    this.isSelectedItemsUnChanged(oldState)
            )
}

