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
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.panel.hPanel
import pl.treksoft.kvision.state.bind
import pl.treksoft.kvision.utils.vw
import se.skoview.app.store
import se.skoview.common.HippoAction
import se.skoview.common.ItemType

// Below is the code to show the different graphs for the advanced version
object AdvancedView : VPanel(
    //FlexDir.ROW, FlexWrap.WRAP, FlexJustify.SPACEBETWEEN, FlexAlignItems.CENTER,
) {
    init {
        /*
        div { }.bind(store) { state ->
            if (state.showTimeGraph && state.historyMap.isNotEmpty()) {
                val animateTime =
                    if (state.currentAction == HippoAction.DoneDownloadHistory::class) {
                        1298
                    } else {
                        -2
                    }

                println("Will display time graph")
                val xAxis = state.historyMap.keys.toList()
                val yAxis = state.historyMap.values.toList()
                chart(
                    Configuration(
                        ChartType.LINE,
                        listOf(
                            DataSets(
                                label = "Antal anrop per dag",
                                data = yAxis
                            )
                        ),
                        xAxis,
                        options = ChartOptions(
                            animation = AnimationOptions(duration = animateTime),
                            legend = LegendOptions(display = true),
                            responsive = true,
                            maintainAspectRatio = false
                        )
                    )
                ).apply {
                    height = 26.vh
                    width = 95.vw
                    //background = Background(Color.name(Col.AZURE))
                }
            }
        }
         */

        // The whole item table
        hPanel(
            spacing = 1
        ) {
            overflow = Overflow.HIDDEN
        }.bind(store) { state ->

            println("Time to update the view...")
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

            add(
                StatPieTableView(
                    itemType = ItemType.CONSUMER,
                    itemSInfoList = SInfo.consumerSInfoList,
                    animateTime = animateTime,
                    label = state.consumerLabel
                )
            )


            add(
                StatPieTableView(
                    itemType = ItemType.CONTRACT,
                    itemSInfoList = SInfo.contractSInfoList,
                    animateTime = animateTime,
                    label = state.contractLabel
                )
            )


            add(
                StatPieTableView(
                    itemType = ItemType.PRODUCER,
                    itemSInfoList = SInfo.producerSInfoList,
                    animateTime = animateTime,
                    label = state.producerLabel
                )
            )


            add(
                StatPieTableView(
                    itemType = ItemType.LOGICAL_ADDRESS,
                    itemSInfoList = SInfo.logicalAddressSInfoList,
                    animateTime = animateTime,
                    label = state.laLabel
                )
            )
        }
    }
}

class StatPieTableView(
    itemType: ItemType,
    itemSInfoList: SInfoList,
    animateTime: Int,
    label: String
) : SimplePanel() {
    init {
        //background = Background(Color.name(Col.GREEN))
        width = 25.vw
        val pieChart =
            Chart(
                getPieChartConfig(
                    itemType,
                    itemSInfoList,
                    animationTime = animateTime
                )
            )
        add(pieChart)
        add(
            ChartLabelTable(
                itemType,
                itemSInfoList.recordList,
                "description",
                "color",
                "calls",
                label
            )
        )
    }
}