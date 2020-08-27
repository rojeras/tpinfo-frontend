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
package se.skoview.view

import se.skoview.data.ItemType

data class StatPreSelect(
    val label: String,
    val selectedItemsMap: HashMap<ItemType, List<Int>>, // = hashMapOf<ItemType, List<Int>>()
    val labelMap: HashMap<ItemType, String>
) {
    init {
        selfStore[label] = this
    }

    companion object {
        val selfStore = hashMapOf<String, StatPreSelect>()

        fun initialize() {
            StatPreSelect(
                "-",
                hashMapOf(),
                hashMapOf(
                    ItemType.CONSUMER to "Applikationer",
                    ItemType.CONTRACT to "Tjänster",
                    ItemType.PRODUCER to "Informationskällor",
                    ItemType.LOGICAL_ADDRESS to "Adresser"
                )
            )
            StatPreSelect(
                "Bokade tider",
                hashMapOf(ItemType.CONTRACT to listOf(117, 118, 114)),
                hashMapOf(
                    ItemType.CONSUMER to "Tidbokningsapplikation",
                    ItemType.CONTRACT to "Typ av bokning",
                    ItemType.PRODUCER to "Tidbokningsystem",
                    ItemType.LOGICAL_ADDRESS to "Enhet"
                )
            )
            StatPreSelect(
                "Skickade remisser",
                hashMapOf(ItemType.CONTRACT to listOf(215)),
                hashMapOf(
                    ItemType.CONSUMER to "Remitterande system",
                    ItemType.CONTRACT to "Remisstyp",
                    ItemType.PRODUCER to "Remissmottagande system",
                    ItemType.LOGICAL_ADDRESS to "Remitterad mottagning"
                )
            )
        }
    }
}




