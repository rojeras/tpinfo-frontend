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
import se.skoview.common.ServiceComponent
import se.skoview.common.ServiceContract

data class itemsFilter(
    val consumers: List<Int> = listOf(),
    val contracts: List<Int> = listOf(),
    val plattformChains: List<Int> = listOf(),
    val producers: List<Int> = listOf(),
    val logicalAdresses: List<Int> = listOf(),
    val domains: List<Int> = listOf()
) {
    val selectedItems: HashMap<ItemType, List<Int>> = hashMapOf(
        ItemType.CONSUMER to consumers,
        ItemType.CONTRACT to contracts,
        ItemType.PLATTFORM_CHAIN to plattformChains,
        ItemType.PRODUCER to producers,
        ItemType.LOGICAL_ADDRESS to logicalAdresses,
        ItemType.DOMAIN to domains
    )
}

data class PreSelect(
    val id: Int,
    val label: String,
    val itemsFilter: itemsFilter,
    val headingsMap: HashMap<ItemType, String>,
    val viewOrder: List<ItemType>,
    val default: Boolean = false
) {
    init {
        mapp[label] = this
        idMapp[id] = this

        if (default) viewPreSelectDefault = this
    }

    companion object {
        val mapp: HashMap<String, PreSelect> = hashMapOf()
        val idMapp: HashMap<Int, PreSelect> = hashMapOf()

        var viewPreSelectDefault: PreSelect? = null

        fun getDefault(): PreSelect? = viewPreSelectDefault
    }
}

fun preSelectInitialize() {

    val allItemsFilter = itemsFilter()

    val journalConsumers: List<Int> = ServiceComponent.mapp
        .filterValues { it.description.contains("Journalen") }
        .map { it.key }

    val ivDomain: List<Int> = ServiceContract.mapp
        .filterValues {
            it.name.equals("ProcessActivity") ||
                it.name.equals("ProcessCareEncounter") ||
                it.name.equals("ProcessCondition") ||
                it.name.equals("ProcessLaboratoryReport") ||
                it.name.equals("ProcessPrescriptionReason")
        }
        .map { it.key }

    val npoConsumers: List<Int> = ServiceComponent.mapp
        .filterValues { it.description.contains("NPÖ") }
        .map { it.key }

    val requestContracts: List<Int> = ServiceContract.mapp
        .filterValues { it.name.equals("ProcessRequest") }
        .map { it.key }

    val timeContracts: List<Int> = ServiceContract.mapp
        .filterValues {
            it.name.contains("MakeBooking") ||
                it.name.contains("UpdateBooking") ||
                it.name.contains("CancelBooking")
        }.map { it.key }

    val timebookingItemsFilter = itemsFilter(contracts = timeContracts)
    val journalItemsFilter = itemsFilter(consumers = journalConsumers)
    // val journalItemsFilter = itemsFilter(consumers = listOf(865))
    val ivItemsFilter = itemsFilter(contracts = ivDomain)
    val npoItemsFilter = itemsFilter(consumers = npoConsumers)
    // val npoItemsFilter = itemsFilter(consumers = listOf(434, 693))
    val reguestItemsFilter = itemsFilter(contracts = requestContracts)
    // val reguestItemsFilter = itemsFilter(contracts = listOf(215))

    PreSelect(
        id = 1,
        label = "Alla konsumerande tjänster",
        itemsFilter = allItemsFilter,
        viewOrder = listOf(ItemType.CONSUMER),
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Applikationer",
            ItemType.CONTRACT to "Tjänster",
            ItemType.PRODUCER to "Informationskällor",
            ItemType.LOGICAL_ADDRESS to "Adresser"
        ),
        default = true
    )

    PreSelect(
        id = 2,
        label = "Anropade producerande tjänster",
        itemsFilter = allItemsFilter,
        viewOrder = listOf(ItemType.PRODUCER),
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Applikationer",
            ItemType.CONTRACT to "Tjänster",
            ItemType.PRODUCER to "Informationskällor",
            ItemType.LOGICAL_ADDRESS to "Adresser"
        ),
        default = false
    )

    PreSelect(
        id = 3,
        label = "Infektionsrapportering",
        itemsFilter = ivItemsFilter,
        viewOrder = listOf(ItemType.LOGICAL_ADDRESS),
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Journalsystem",
            ItemType.CONTRACT to "Informationsmängder",
            ItemType.PRODUCER to "Infektionsverktyg",
            ItemType.LOGICAL_ADDRESS to "Rapporterande vårdgivare"
        )
    )

    PreSelect(
        id = 4,
        label = "Journalen",
        itemsFilter = journalItemsFilter,
        viewOrder = listOf(ItemType.CONTRACT),
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Applikation",
            ItemType.CONTRACT to "Informationsmängder",
            ItemType.PRODUCER to "Journalsystem",
            ItemType.LOGICAL_ADDRESS to "Journalsystemets adress"
        )
    )

    PreSelect(
        id = 5,
        label = "Nationell patientöversikt (NPÖ)",
        itemsFilter = npoItemsFilter,
        viewOrder = listOf(ItemType.CONTRACT),
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Applikation",
            ItemType.CONTRACT to "Informationsmängder",
            ItemType.PRODUCER to "Journalsystem",
            ItemType.LOGICAL_ADDRESS to "Journalsystemets adress"
        )
    )

    PreSelect(
        id = 6,
        label = "Remisser",
        itemsFilter = reguestItemsFilter,
        viewOrder = listOf(ItemType.LOGICAL_ADDRESS),
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Remitterande system",
            ItemType.CONTRACT to "Remisstyp",
            ItemType.PRODUCER to "Remissmottagande system",
            ItemType.LOGICAL_ADDRESS to "Remitterad mottagning"
        )
    )

    PreSelect(
        id = 7,
        label = "Tidbokningar",
        itemsFilter = timebookingItemsFilter,
        viewOrder = listOf(ItemType.LOGICAL_ADDRESS),
        headingsMap = hashMapOf(
            ItemType.CONSUMER to "Tidbokningsapplikation",
            ItemType.CONTRACT to "Typ av bokning",
            ItemType.PRODUCER to "Tidbokningsystem",
            ItemType.LOGICAL_ADDRESS to "Enhet"
        )
    )
}
