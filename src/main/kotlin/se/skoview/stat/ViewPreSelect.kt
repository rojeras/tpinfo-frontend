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

data class itemsFilter(
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
    val itemsFilter: itemsFilter,
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
    val itemsFilter: itemsFilter,
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

    val allItemsFilter = itemsFilter()

    // for ((key, value) in ServiceContract.mapp) { }
    val timeContracts: List<Int> = ServiceContract.mapp
        .filterValues {
            it.name.contains("MakeBooking") ||
                it.name.contains("UpdateBooking") ||
                it.name.contains("CancelBooking")
        }
        .map { it.key }

    val timebookingItemsFilter = itemsFilter(contracts = timeContracts)

    val journalItemsFilter = itemsFilter(consumers = listOf(865))
    val npoItemsFilter = itemsFilter(consumers = listOf(434, 693))
    val reguestItemsFilter = itemsFilter(contracts = listOf(215))

    SimpleViewPreSelect(
        label = SIMPLE_VIEW_DEFAULT_PRESELECT,
        itemsFilter = allItemsFilter, // hashMapOf(),
        simpleModeViewOrder = listOf(ItemType.CONSUMER),
        default = true
    )

    SimpleViewPreSelect(
        label = "Anropade producerande tjänster",
        itemsFilter = allItemsFilter,
        simpleModeViewOrder = listOf(ItemType.PRODUCER)
    )

    SimpleViewPreSelect(
        label = "Tidbokningar",
        itemsFilter = timebookingItemsFilter,
        simpleModeViewOrder = listOf(ItemType.LOGICAL_ADDRESS),
    )

    SimpleViewPreSelect(
        label = "Journalen",
        itemsFilter = journalItemsFilter,
        simpleModeViewOrder = listOf(ItemType.CONTRACT)
    )

    SimpleViewPreSelect(
        label = "Nationell patientöversikt (NPÖ)",
        itemsFilter = npoItemsFilter,
        simpleModeViewOrder = listOf(ItemType.CONTRACT),
    )

    SimpleViewPreSelect(
        label = "Remisser",
        itemsFilter = reguestItemsFilter,
        simpleModeViewOrder = listOf(ItemType.LOGICAL_ADDRESS)
    )

    AdvancedViewPreSelect(
        label = ADVANCED_VIEW_DEFAULT_PRESELECT,
        itemsFilter = allItemsFilter,
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
        itemsFilter = itemsFilter(contracts = listOf(117, 118, 114)),
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Tidbokningsapplikation",
            ItemType.CONTRACT to "Typ av bokning",
            ItemType.PRODUCER to "Tidbokningsystem",
            ItemType.LOGICAL_ADDRESS to "Enhet"
        )
    )

    AdvancedViewPreSelect(
        label = "Journalen",
        itemsFilter = journalItemsFilter,
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Applikation",
            ItemType.CONTRACT to "Information",
            ItemType.PRODUCER to "Journalsystem",
            ItemType.LOGICAL_ADDRESS to "Journalsystemets adress"
        )
    )

    AdvancedViewPreSelect(
        label = "Nationell patientöversikt (NPÖ)",
        itemsFilter = npoItemsFilter,
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Applikation",
            ItemType.CONTRACT to "Information",
            ItemType.PRODUCER to "Journalsystem",
            ItemType.LOGICAL_ADDRESS to "Journalsystemets adress"
        )
    )

    AdvancedViewPreSelect(
        label = "Remisser",
        itemsFilter = reguestItemsFilter,
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Remitterande system",
            ItemType.CONTRACT to "Remisstyp",
            ItemType.PRODUCER to "Remissmottagande system",
            ItemType.LOGICAL_ADDRESS to "Remitterad mottagning"
        )
    )
}
