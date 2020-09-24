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

import se.skoview.app.store
import se.skoview.common.HippoAction
import se.skoview.common.ItemType

data class AdvancedPreSelect(
    val label: String,
    val simpleLabel: String? = null,
    val selectedItemsMap: HashMap<ItemType, List<Int>>, // = hashMapOf<ItemType, List<Int>>()
    val labelMap: HashMap<ItemType, String>,
    val simpleViewDisplay: ItemType? = null, // todo: Modify to array if we need tp extend the simple view
    val showInSimpleView: Boolean,
    val showInAdvancedView: Boolean

) {
    fun getLabel(advancedMode: Boolean) =
        if ((!advancedMode) && simpleLabel != null) simpleLabel
        else
            label


    init {
        mapp[label] = this
    }

    companion object {
        val mapp = mutableMapOf<String, AdvancedPreSelect>()

        fun initialize() {
            AdvancedPreSelect(
                label = "Allt",
                simpleLabel = "Alla konsumerande tjänster",
                selectedItemsMap = hashMapOf(),
                labelMap = hashMapOf(
                    ItemType.CONSUMER to "Applikationer",
                    ItemType.CONTRACT to "Tjänster",
                    ItemType.PRODUCER to "Informationskällor",
                    ItemType.LOGICAL_ADDRESS to "Adresser"
                ),
                simpleViewDisplay = ItemType.CONSUMER,
                showInSimpleView = true,
                showInAdvancedView = true
            )
            AdvancedPreSelect(
                label = "--",
                simpleLabel = "Anropade producerande tjänster",
                selectedItemsMap = hashMapOf(),
                labelMap = hashMapOf(
                    ItemType.CONSUMER to "Applikationer",
                    ItemType.CONTRACT to "Tjänster",
                    ItemType.PRODUCER to "Informationskällor",
                    ItemType.LOGICAL_ADDRESS to "Adresser"
                ),
                simpleViewDisplay = ItemType.PRODUCER,
                showInSimpleView = true,
                showInAdvancedView = false
            )
            AdvancedPreSelect(
                label = "Bokade tider",
                selectedItemsMap = hashMapOf(ItemType.CONTRACT to listOf(117, 118, 114)),
                labelMap = hashMapOf(
                    ItemType.CONSUMER to "Tidbokningsapplikation",
                    ItemType.CONTRACT to "Typ av bokning",
                    ItemType.PRODUCER to "Tidbokningsystem",
                    ItemType.LOGICAL_ADDRESS to "Enhet"
                ),
                simpleViewDisplay = ItemType.LOGICAL_ADDRESS,
                showInSimpleView = true,
                showInAdvancedView = true
            )
            AdvancedPreSelect(
                label = "Journalen",
                selectedItemsMap = hashMapOf(ItemType.CONSUMER to listOf(865)),
                labelMap = hashMapOf(
                    ItemType.CONSUMER to "Applikation",
                    ItemType.CONTRACT to "Information",
                    ItemType.PRODUCER to "Journalsystem",
                    ItemType.LOGICAL_ADDRESS to "Journalsystemets adress"
                ),
                simpleViewDisplay = ItemType.CONTRACT,
                showInSimpleView = true,
                showInAdvancedView = true
            )
            AdvancedPreSelect(
                label = "Nationell patientöversikt (NPÖ)",
                selectedItemsMap = hashMapOf(ItemType.CONSUMER to listOf(434, 693)),
                labelMap = hashMapOf(
                    ItemType.CONSUMER to "Applikation",
                    ItemType.CONTRACT to "Information",
                    ItemType.PRODUCER to "Journalsystem",
                    ItemType.LOGICAL_ADDRESS to "Journalsystemets adress"
                ),
                simpleViewDisplay = ItemType.CONTRACT,
                showInSimpleView = true,
                showInAdvancedView = true
            )
            AdvancedPreSelect(
                label = "Remisser",
                selectedItemsMap = hashMapOf(ItemType.CONTRACT to listOf(215)),
                labelMap = hashMapOf(
                    ItemType.CONSUMER to "Remitterande system",
                    ItemType.CONTRACT to "Remisstyp",
                    ItemType.PRODUCER to "Remissmottagande system",
                    ItemType.LOGICAL_ADDRESS to "Remitterad mottagning"
                ),
                simpleViewDisplay = ItemType.LOGICAL_ADDRESS,
                showInSimpleView = true,
                showInAdvancedView = true
            )
        }
    }
}

fun selectPreSelect(selectedPreSelectLabel: String) {
    println("Selected pre-select: '$selectedPreSelectLabel'")

    val preLabel =
        if (selectedPreSelectLabel.equals("default")) "Allt"
        else selectedPreSelectLabel

    val preSelect = AdvancedPreSelect.mapp[preLabel]!!
    store.dispatch(HippoAction.PreSelectedSet(preSelect))

    if (store.getState().showTechnicalTerms) { // Restore technical labels
        store.dispatch(HippoAction.ShowTechnicalTerms(store.getState().showTechnicalTerms))
    }

    store.dispatch(HippoAction.ItemDeselectedAllForAllTypes)

    val selectedItemsMap = preSelect.selectedItemsMap
    for ((itemType, itemIdList) in selectedItemsMap) {
        itemIdList.forEach {
            store.dispatch(HippoAction.ItemIdSelected(itemType, it))
        }
    }
    loadStatistics(store.getState())
}




