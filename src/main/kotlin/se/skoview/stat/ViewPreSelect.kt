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
package se.skoview.stat

import se.skoview.common.ItemType
import se.skoview.common.ServiceContract

const val SIMPLE_VIEW_DEFAULT_PRESELECT: String = "Alla konsumerande tjänster"
const val ADVANCED_VIEW_DEFAULT_PRESELECT: String = "Allt"

data class FilteredItems(
    val consumers: List<Int> = listOf(),
    val contracts: List<Int> = listOf(),
    val plattformChains: List<Int> = listOf(),
    val producers: List<Int> = listOf(),
    val logicalAdresses: List<Int> = listOf()
) {
    val selectedItems: HashMap<ItemType, List<Int>> = hashMapOf(
        ItemType.CONSUMER to consumers,
        ItemType.CONTRACT to contracts,
        ItemType.PLATTFORM_CHAIN to plattformChains,
        ItemType.PRODUCER to producers,
        ItemType.LOGICAL_ADDRESS to logicalAdresses
    )
}


interface ViewPreSelect

data class SimpleViewPreSelect(
    val label: String,
    val filteredItems: FilteredItems,
    val simpleModeViewOrder: List<ItemType>,
    val default: Boolean = false
) : ViewPreSelect {
    init {
        mapp[label] = this

        if (default) simpleViewPreSelectDefault = this
    }

    companion object {
        val mapp: HashMap<String, SimpleViewPreSelect> = hashMapOf()

        var simpleViewPreSelectDefault: SimpleViewPreSelect? = null

        fun getDefault(): SimpleViewPreSelect? = simpleViewPreSelectDefault
    }
}

data class AdvancedViewPreSelect(
    val label: String,
    val filteredItems: FilteredItems,
    val headingsMap: HashMap<ItemType, String>,
    val default: Boolean = false
) : ViewPreSelect {
    init {
        mapp[label] = this

        if (default) advancedViewPreSelectDefault = this
    }

    companion object {
        val mapp: HashMap<String, AdvancedViewPreSelect> = hashMapOf()

        var advancedViewPreSelectDefault: AdvancedViewPreSelect? = null

        fun getDefault(): AdvancedViewPreSelect? = advancedViewPreSelectDefault
    }
}

fun viewPreSelectInitialize() {

    val allItemsFilter = FilteredItems()

    // for ((key, value) in ServiceContract.mapp) { }
    val timeContracts: List<Int> = ServiceContract.mapp
        .filterValues {
            it.name.contains("MakeBooking") ||
                    it.name.contains("UpdateBooking") ||
                    it.name.contains("CancelBooking")
        }
        .map { it.key }

    val timebookingItemsFilter = FilteredItems(contracts = timeContracts)

    val journalItemsFilter = FilteredItems(consumers = listOf(865))
    val npoItemsFilter = FilteredItems(consumers = listOf(434, 693))
    val reguestItemsFilter = FilteredItems(contracts = listOf(215))

    SimpleViewPreSelect(
        label = SIMPLE_VIEW_DEFAULT_PRESELECT,
        filteredItems = allItemsFilter, // hashMapOf(),
        simpleModeViewOrder = listOf(ItemType.CONSUMER),
        default = true
    )

    SimpleViewPreSelect(
        label = "Anropade producerande tjänster",
        filteredItems = allItemsFilter,
        simpleModeViewOrder = listOf(ItemType.PRODUCER)
    )

    SimpleViewPreSelect(
        label = "Tidbokningar",
        filteredItems = timebookingItemsFilter,
        simpleModeViewOrder = listOf(ItemType.LOGICAL_ADDRESS),
    )

    SimpleViewPreSelect(
        label = "Journalen",
        filteredItems = journalItemsFilter,
        simpleModeViewOrder = listOf(ItemType.CONTRACT)
    )

    SimpleViewPreSelect(
        label = "Nationell patientöversikt (NPÖ)",
        filteredItems = npoItemsFilter,
        simpleModeViewOrder = listOf(ItemType.CONTRACT),
    )

    SimpleViewPreSelect(
        label = "Remisser",
        filteredItems = reguestItemsFilter,
        simpleModeViewOrder = listOf(ItemType.LOGICAL_ADDRESS)
    )


    AdvancedViewPreSelect(
        label = ADVANCED_VIEW_DEFAULT_PRESELECT,
        filteredItems = allItemsFilter,
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Applikationer",
            ItemType.CONTRACT to "Tjänster",
            ItemType.PRODUCER to "Informationskällor",
            ItemType.LOGICAL_ADDRESS to "Adresser"
        ),
        default = true
    )
    AdvancedViewPreSelect(
        label = "Tidbokningar",
        filteredItems = FilteredItems(contracts = listOf(117, 118, 114)),
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Tidbokningsapplikation",
            ItemType.CONTRACT to "Typ av bokning",
            ItemType.PRODUCER to "Tidbokningsystem",
            ItemType.LOGICAL_ADDRESS to "Enhet"
        )
    )

    AdvancedViewPreSelect(
        label = "Journalen",
        filteredItems = journalItemsFilter,
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Applikation",
            ItemType.CONTRACT to "Information",
            ItemType.PRODUCER to "Journalsystem",
            ItemType.LOGICAL_ADDRESS to "Journalsystemets adress"
        )
    )

    AdvancedViewPreSelect(
        label = "Nationell patientöversikt (NPÖ)",
        filteredItems = npoItemsFilter,
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Applikation",
            ItemType.CONTRACT to "Information",
            ItemType.PRODUCER to "Journalsystem",
            ItemType.LOGICAL_ADDRESS to "Journalsystemets adress"
        )
    )

    AdvancedViewPreSelect(
        label = "Remisser",
        filteredItems = reguestItemsFilter,
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Remitterande system",
            ItemType.CONTRACT to "Remisstyp",
            ItemType.PRODUCER to "Remissmottagande system",
            ItemType.LOGICAL_ADDRESS to "Remitterad mottagning"
        )
    )

}
