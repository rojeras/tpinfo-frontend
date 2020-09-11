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

import pl.treksoft.kvision.chart.Chart
import pl.treksoft.kvision.core.Overflow
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.flexPanel
import pl.treksoft.kvision.state.bind
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.vw
import se.skoview.app.store
import se.skoview.common.HippoAction
import se.skoview.common.ItemType

// Below is the code to show the different graphs for the simple version
object SimpleView : SimplePanel(
    //FlexDir.ROW, FlexWrap.WRAP, FlexJustify.SPACEBETWEEN, FlexAlignItems.CENTER,
) {
    init {
        id = "SimpleView"
        //background = Background(Color.name(Col.RED))
        // The whole item table
        flexPanel(
        ) {
            //spacing = 1
            overflow = Overflow.HIDDEN
            id = "SimpleViewInnerhPanel"
            width = 100.vw
            //background = Background(Color.name(Col.CHOCOLATE))
        }.bind(store) { state ->
            println("Time to update the simple view...")
            SInfo.createStatViewData(state)

            val animateTime =
                if (state.currentAction == HippoAction.DoneDownloadStatistics::class) {
                    //SInfo.createStatViewData(state)
                    println("Chart will now change")
                    1299
                } else {
                    println("Chart will NOT change")
                    -1
                }

            //val itemType: ItemType = ItemType.CONSUMER
            // todo: Make the !! go away
            val currentPreSelect: StatPreSelect = StatPreSelect.selfStore[state.statPreSelect]!!
            val itemType: ItemType = currentPreSelect.showInSimpleView!!

            var itemSInfoList: SInfoList
            var label: String

            when (itemType) {
                ItemType.CONSUMER -> {
                    itemSInfoList = SInfo.consumerSInfoList
                    label = state.consumerLabel
                }
                ItemType.CONTRACT -> {
                    itemSInfoList = SInfo.contractSInfoList
                    label = state.contractLabel
                }
                ItemType.PRODUCER -> {
                    itemSInfoList = SInfo.producerSInfoList
                    label = state.producerLabel
                }
                ItemType.LOGICAL_ADDRESS -> {
                    itemSInfoList = SInfo.logicalAddressSInfoList
                    label = state.laLabel
                }
                else -> {
                    println("ERROR in TheSimpleView, itemType = $itemType")
                    itemSInfoList = SInfo.consumerSInfoList
                    label = state.consumerLabel
                }
            }

            id = "SimpleStatPieTableView"
            //background = Background(Color.name(Col.GREEN))
            width = 100.vw
            val pieChart =
                Chart(
                    getPieChartConfig(
                        itemType,
                        itemSInfoList,
                        animationTime = animateTime,
                        responsive = true,
                        maintainAspectRatio = false
                    )
                )
            add(
                pieChart.apply {
                    width = 40.vw
                    height = 80.perc
                    id = "pieChart"
                    margin = 5.vw
                    //background = Background(Color.name(Col.ALICEBLUE))
                }, grow = 1
            ).apply {
                id = "The added pie container"
            }
            add(
                ChartLabelTable(
                    itemType,
                    itemSInfoList.recordList,
                    "description",
                    "color",
                    "calls",
                    label
                ).apply {
                    id = "ChartLabelTable"
                    width = 40.vw
                    margin = 5.vw
                    //background = Background(Color.name(Col.LAWNGREEN))
                }, grow = 1
            )
        }.apply { id = "postId" }
    }
}
