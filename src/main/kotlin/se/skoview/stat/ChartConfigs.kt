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

import com.github.snabbdom._get
import pl.treksoft.kvision.chart.*
import se.skoview.app.store
import se.skoview.common.HippoAction
import se.skoview.common.ItemType
import se.skoview.common.isItemSelected


fun getPieChartConfig(
    itemType: ItemType,
    itemSInfoList: SInfoList,
    animationTime: Int = 0
): Configuration {
    val configuration = Configuration(
        ChartType.PIE,
        listOf(
            DataSets(
                data = itemSInfoList.callList(),
                backgroundColor = itemSInfoList.colorList()
            )
        ),
        itemSInfoList.descList(),
        options = ChartOptions(
            elements = ElementsOptions(arc = ArcOptions(borderWidth = 0)),
            animation = AnimationOptions(duration = animationTime),
            //responsive = false,
            legend = LegendOptions(display = false),
            onClick = { _, activeElements ->
                val sliceIx = activeElements[0]._get("_index") as Int
                val itemId: Int = itemSInfoList.recordList[sliceIx].itemId
                if (store.getState().isItemSelected(itemType, itemId)) {
                    store.dispatch { _, getState ->
                        store.dispatch(HippoAction.ItemIdDeselectedAll(itemType))
                        loadStatistics(getState())
                    }
                } else {
                    store.dispatch { _, getState ->
                        store.dispatch(HippoAction.ItemIdSelected(itemType, itemId))
                        loadStatistics(getState())
                    }

                }
                "" // This lambda returns Any, which mean the last line must be an expression
            }
        )
    )
    return configuration
}

fun getLineChartConfig(
    historyMap: Map<String, Int>,
    animationTime: Int = 0
): Configuration {
    val configuration = Configuration(
        ChartType.LINE,
        listOf(
            DataSets(
                label = "Antal anrop per dag",
                data = historyMap.values.toList()
            )
        ),
        historyMap.keys.toList(),
        options = ChartOptions(
            animation = AnimationOptions(duration = animationTime),
            //responsive = false
            maintainAspectRatio = false
        )
    )
    return configuration
}