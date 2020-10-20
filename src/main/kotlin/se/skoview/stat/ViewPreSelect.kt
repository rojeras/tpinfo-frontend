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

val ALL_ITEMS_FILTER = FilteredItems()
val TIMEBOOKING_ITEMS_FILTER = FilteredItems(contracts = listOf(117, 118, 114))
val JOURNAL_ITEMS_FILTER = FilteredItems(consumers = listOf(865))
val NPO_ITEMS_FILTER = FilteredItems(consumers = listOf(434, 693))
val REQUEST_ITEMS_FILTER = FilteredItems(contracts = listOf(215))

data class SimpleViewPreSelect(
    val label: String,
    val filteredItems: FilteredItems,
    val simpleModeViewOrder: List<ItemType>
) {
    init {
        mapp[label] = this
    }

    companion object {
        val mapp: HashMap<String, SimpleViewPreSelect> = hashMapOf()

        fun getDefault(): SimpleViewPreSelect = simpleViewPreSelectDefault
    }
}

data class AdvancedViewPreSelect(
    val label: String,
    val filteredItems: FilteredItems,
    val headingsMap: HashMap<ItemType, String>,
) {
    init {
        mapp[label] = this
    }

    companion object {
        val mapp: HashMap<String, AdvancedViewPreSelect> = hashMapOf()

        fun getDefault(): AdvancedViewPreSelect = advancedViewPreSelectDefault
    }
}

val simpleViewPreSelectDefault: SimpleViewPreSelect =
    SimpleViewPreSelect(
        label = SIMPLE_VIEW_DEFAULT_PRESELECT,
        filteredItems = ALL_ITEMS_FILTER,   //hashMapOf(),
        simpleModeViewOrder = listOf(ItemType.CONSUMER)
    )

val simplePreSelects = listOf<SimpleViewPreSelect>(
    simpleViewPreSelectDefault,
    SimpleViewPreSelect(
        label = "Anropade producerande tjänster",
        filteredItems = ALL_ITEMS_FILTER,
        simpleModeViewOrder = listOf(ItemType.PRODUCER)
    ),
    SimpleViewPreSelect(
        label = "Tidbokningar",
        filteredItems = TIMEBOOKING_ITEMS_FILTER,
        simpleModeViewOrder = listOf(ItemType.LOGICAL_ADDRESS),
    ),
    SimpleViewPreSelect(
        label = "Journalen",
        filteredItems = JOURNAL_ITEMS_FILTER,
        simpleModeViewOrder = listOf(ItemType.CONTRACT)
    ),
    SimpleViewPreSelect(
        label = "Nationell patientöversikt (NPÖ)",
        filteredItems = NPO_ITEMS_FILTER,
        simpleModeViewOrder = listOf(ItemType.CONTRACT),
    ),
    SimpleViewPreSelect(
        label = "Remisser",
        filteredItems = REQUEST_ITEMS_FILTER,
        simpleModeViewOrder = listOf(ItemType.LOGICAL_ADDRESS)
    ),
)

val advancedViewPreSelectDefault =
    AdvancedViewPreSelect(
        label = ADVANCED_VIEW_DEFAULT_PRESELECT,
        filteredItems = ALL_ITEMS_FILTER,
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Applikationer",
            ItemType.CONTRACT to "Tjänster",
            ItemType.PRODUCER to "Informationskällor",
            ItemType.LOGICAL_ADDRESS to "Adresser"
        )
    )
val advancedViewPreSelects = listOf<AdvancedViewPreSelect>(
    advancedViewPreSelectDefault,
    AdvancedViewPreSelect(
        label = "Tidbokningar",
        filteredItems = FilteredItems(contracts = listOf(117, 118, 114)),
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Tidbokningsapplikation",
            ItemType.CONTRACT to "Typ av bokning",
            ItemType.PRODUCER to "Tidbokningsystem",
            ItemType.LOGICAL_ADDRESS to "Enhet"
        )
    ),
    AdvancedViewPreSelect(
        label = "Journalen",
        filteredItems = JOURNAL_ITEMS_FILTER,
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Applikation",
            ItemType.CONTRACT to "Information",
            ItemType.PRODUCER to "Journalsystem",
            ItemType.LOGICAL_ADDRESS to "Journalsystemets adress"
        )
    ),
    AdvancedViewPreSelect(
        label = "Nationell patientöversikt (NPÖ)",
        filteredItems = NPO_ITEMS_FILTER,
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Applikation",
            ItemType.CONTRACT to "Information",
            ItemType.PRODUCER to "Journalsystem",
            ItemType.LOGICAL_ADDRESS to "Journalsystemets adress"
        )
    ),
    AdvancedViewPreSelect(
        label = "Remisser",
        filteredItems = REQUEST_ITEMS_FILTER,
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Remitterande system",
            ItemType.CONTRACT to "Remisstyp",
            ItemType.PRODUCER to "Remissmottagande system",
            ItemType.LOGICAL_ADDRESS to "Remitterad mottagning"
        )
    ),
)





