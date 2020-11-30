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
import se.skoview.stat.SimpleViewPreSelect
import se.skoview.stat.StatisticsBlob
import se.skoview.stat.simpleViewPreSelectDefault
import kotlin.reflect.KClass

enum class AsyncActionStatus {
    NOT_INITIALIZED,
    INITIALIZED,
    COMPLETED,
    ERROR
}

enum class DateType {
    EFFECTIVE,
    END,
    EFFECTIVE_AND_END,
    STAT_EFFECTIVE,
    STAT_END
}

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
    // val viewMode: ViewMode = ViewMode.SIMPLE,
    val simpleViewPreSelect: SimpleViewPreSelect = simpleViewPreSelectDefault,
    val advancedViewPreSelect: AdvancedViewPreSelect? = null
)

fun initializeHippoState(): HippoState {
    val datesPair = getDatesLastMonth()
    val statDateEffective = datesPair.first.toSwedishDate()
    val statDateEnd = datesPair.second.toSwedishDate()

    return HippoState(
        statDateEffective = statDateEffective,
        statDateEnd = statDateEnd,
    )
}

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

fun HippoState.isStatPlattformSelected(): Boolean {
    // return isStatPlattformInPlattformChainList(this.selectedPlattformChainsIds, this.statisticsPlattforms)
    return isStatPlattformInPlattformChainList(this.selectedPlattformChainsIds, StatisticsPlattform.mapp)
}

private fun isStatPlattformInPlattformChainList(plattformChains: List<Int>, statisticsPlattforms: Map<Int, StatisticsPlattform>): Boolean {
    if (plattformChains.size != 1) return false
    val selectedPlattformChainId = plattformChains[0]
    val selectedPlattformChain = PlattformChain.mapp[selectedPlattformChainId]!!

    if (
        statisticsPlattforms.contains(selectedPlattformChain.first) ||
        statisticsPlattforms.contains(selectedPlattformChain.last)
    ) return true

    return false
}

fun HippoState.isPlattformSelected(id: Int): Boolean {
    val pChainId = PlattformChain.calculateId(first = id, middle = null, last = id)

    return this.selectedPlattformChainsIds.contains(pChainId)
}

// The extension function create the part of the URL to fetch integrations
fun HippoState.getParams(view: View): String {

    // var params = "?dummy&contractId=379"
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

// is HippoAction.ItemIdSelected -> {
fun HippoState.itemIdSeclected(id: Int, viewType: ItemType): HippoState {

    return when (viewType) {
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

// is HippoAction.ItemIdDeselected -> {
fun HippoState.itemIdDeseclected(id: Int, viewType: ItemType): HippoState {
    return when (viewType) {
        ItemType.CONSUMER -> {
            val newList = this.selectedConsumersIds as MutableList<Int>
            newList.remove(id)
            this.copy(
                selectedConsumersIds = newList,
                advancedViewPreSelect = null
            )
        }
        ItemType.DOMAIN -> {
            val newList = this.selectedDomainsIds as MutableList<Int>
            newList.remove(id)
            this.copy(
                selectedDomainsIds = newList,
                advancedViewPreSelect = null
            )
        }
        ItemType.CONTRACT -> {
            val newList = this.selectedContractsIds as MutableList<Int>
            newList.remove(id)
            this.copy(
                selectedContractsIds = newList,
                advancedViewPreSelect = null
            )
        }
        ItemType.PLATTFORM_CHAIN -> {
            val newList = this.selectedPlattformChainsIds as MutableList<Int>
            newList.remove(id)
            this.copy(
                selectedPlattformChainsIds = newList,
                advancedViewPreSelect = null
            )
        }
        ItemType.LOGICAL_ADDRESS -> {
            val newList = this.selectedLogicalAddressesIds as MutableList<Int>
            newList.remove(id)
            this.copy(
                selectedLogicalAddressesIds = newList,
                advancedViewPreSelect = null
            )
        }
        ItemType.PRODUCER -> {
            val newList = this.selectedProducersIds as MutableList<Int>
            newList.remove(id)
            this.copy(
                selectedProducersIds = newList,
                advancedViewPreSelect = null
            )
        }
    }
}

// is HippoAction.StatTpSelected -> {
fun HippoState.statTpSelected(tpId: Int): HippoState {
    // TP can only be selected in advanced mode

    val preSelect: AdvancedViewPreSelect = AdvancedViewPreSelect.getDefault()
    val pChainId = PlattformChain.calculateId(first = tpId, middle = null, last = tpId)

    return this.copy(
        selectedPlattformChainsIds = listOf(pChainId),
        advancedViewPreSelect = preSelect
    )
}

// is HippoAction.SetView -> {
fun HippoState.setNewView(newView: View): HippoState {

    val currentView = this.view
    // if (currentView == newView) throw RuntimeException("Current view  == new  in reducer SetViewMode")
    if (currentView == newView) return this

    // If the new mode have a preselect with the same label as the current, apply it. Otherwise use its default.
    if (
        currentView == View.STAT_ADVANCED &&
        newView == View.STAT_SIMPLE
    ) {
        val currentAdvancedPreSelect = this.advancedViewPreSelect ?: AdvancedViewPreSelect.getDefault()
        val currentAdvancedPreSelectLabel = currentAdvancedPreSelect.label
        val newSimpleViewPreSelect =
            SimpleViewPreSelect.mapp[currentAdvancedPreSelectLabel] ?: SimpleViewPreSelect.getDefault()
        return applyFilteredItemsSelection(
            this,
            newSimpleViewPreSelect.filteredItems
        ).copy(
            simpleViewPreSelect = newSimpleViewPreSelect,
            view = newView
        )
    }

    if (currentView == View.STAT_SIMPLE &&
        newView == View.STAT_ADVANCED
    ) {
        val currentSimplePreSelectLabel = this.simpleViewPreSelect.label
        val newAdvancedViewPreSelect =
            AdvancedViewPreSelect.mapp[currentSimplePreSelectLabel] ?: AdvancedViewPreSelect.getDefault()
        return applyFilteredItemsSelection(
            this,
            newAdvancedViewPreSelect.filteredItems
        ).copy(
            advancedViewPreSelect = newAdvancedViewPreSelect,
            view = newView
        )
    }

    // Switch from hippo to statistics
    if (
        currentView == View.HIPPO &&
        (newView == View.STAT_SIMPLE || newView == View.STAT_ADVANCED)
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
        (currentView == View.STAT_SIMPLE || currentView == View.STAT_ADVANCED) &&
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

/*
fun HippoState.setView(newView: View): HippoState {

    val currentView = this.view
    if (currentView == newView) throw RuntimeException("Current view  == new  in reducer SetViewMode")

    // If the new mode have a preselect with the same label as the current, apply it. Otherwise use its default.
    return when (newView) {
        View.STAT_SIMPLE -> {
            val currentAdvancedPreSelect = this.advancedViewPreSelect ?: AdvancedViewPreSelect.getDefault()
            val currentAdvancedPreSelectLabel = currentAdvancedPreSelect.label
            val newSimpleViewPreSelect =
                SimpleViewPreSelect.mapp[currentAdvancedPreSelectLabel] ?: SimpleViewPreSelect.getDefault()
            applyFilteredItemsSelection(
                this,
                newSimpleViewPreSelect.filteredItems
            ).copy(
                simpleViewPreSelect = newSimpleViewPreSelect,
                view = View.STAT_SIMPLE
            )
        }
        View.STAT_ADVANCED -> {
            val currentSimplePreSelectLabel = this.simpleViewPreSelect.label
            val newAdvancedViewPreSelect =
                AdvancedViewPreSelect.mapp[currentSimplePreSelectLabel] ?: AdvancedViewPreSelect.getDefault()
            applyFilteredItemsSelection(this, newAdvancedViewPreSelect.filteredItems).copy(
                advancedViewPreSelect = newAdvancedViewPreSelect,
                view = View.STAT_ADVANCED
            )
        }
        else -> this.copy(view = View.HIPPO)
    }
}
*/
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

// is HippoAction.ApplyBookmark -> {
fun HippoState.applyBookmark(view: View, bookmark: BookmarkInformation): HippoState {

    val newState =

        if (view == View.HIPPO) {
            val newDateEffective: String? =
                if (bookmark.dateEffective != null) bookmark.dateEffective
                else this.dateEffective
            val newDateEnd: String? =
                if (bookmark.dateEnd != null) bookmark.dateEnd
                else this.dateEnd

            this.copy(
                dateEffective = newDateEffective,
                dateEnd = newDateEnd,
            )
        } else {
            val datesLastMonth = getDatesLastMonth()

            val newDateEffective: String =
                if (bookmark.dateEffective != null) bookmark.dateEffective!!
                else datesLastMonth.first.toSwedishDate()

            val newDateEnd: String =
                if (bookmark.dateEnd != null) bookmark.dateEnd!!
                else datesLastMonth.second.toSwedishDate()

            this.copy(
                statDateEffective = newDateEffective,
                statDateEnd = newDateEnd,
            )
        }

    return newState.copy(
        view = view,
        selectedConsumersIds = bookmark.selectedConsumers,
        selectedProducersIds = bookmark.selectedProducers,
        selectedLogicalAddressesIds = bookmark.selectedLogicalAddresses,
        selectedContractsIds = bookmark.selectedContracts,
        selectedDomainsIds = bookmark.selectedDomains,
        selectedPlattformChainsIds = bookmark.selectedPlattformChains
    )
}
