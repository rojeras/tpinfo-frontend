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
import pl.treksoft.kvision.core.Background
import pl.treksoft.kvision.core.Col
import pl.treksoft.kvision.core.Color
import pl.treksoft.kvision.core.Overflow
import pl.treksoft.kvision.html.div
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.flexPanel
import pl.treksoft.kvision.state.bind
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.vw
import se.skoview.app.store
import se.skoview.common.HippoAction
import se.skoview.common.ItemType
import se.skoview.common.getHeightToRemainingViewPort

// Below is the code to show the different graphs for the simple version
object SimpleView : SimplePanel(
) {
    init {
        id = "TheSimpleView:SimplePanel"
        //background = Background(Color.name(Col.RED))
        //height = 100.perc
        div { }.bind(store) { state ->
            id = "TheSimpleView:SimplePanel-Bind"
            height = 100.perc
            background = Background(Color.name(Col.LIGHTBLUE))


            flexPanel(
            ) {
                //spacing = 1
                id = "TheSimpleViewBigPanel:FlexPanel"
                overflow = Overflow.HIDDEN
                width = 100.vw

                //val occupiedViewPortArea = (statPageTop.getElementJQuery()?.innerHeight() ?: 153).toInt()
                /*
                val occupiedViewPortArea = (statPageTop.getElementJQuery()?.height() ?: 153).toInt()
                println("+++++++++- Inner height: $occupiedViewPortArea")
                val heightToRemove = occupiedViewPortArea + 40
                setStyle("height", "calc(100vh - ${heightToRemove}px)")
                 */
                //setStyle("height", "calc(100vh - 200px)")

                setStyle("height", getHeightToRemainingViewPort(statPageTop, 40))

                height = 100.perc
                background = Background(Color.name(Col.YELLOW))

                SInfo.createStatViewData(state)

                val animateTime =
                    if (state.currentAction == HippoAction.DoneDownloadStatistics::class) {
                        1299
                    } else {
                        -1
                    }

                //val itemType: ItemType = ItemType.CONSUMER
                // todo: Make the !! go away
                val currentPreSelect: StatPreSelect = StatPreSelect.mapp[state.statPreSelect]!!
                val itemType: ItemType = currentPreSelect.simpleViewDisplay!!

                val itemSInfoList: SInfoList
                val label: String

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

                //width = 100.vw
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
                        id = "TheSimpleViewPieChart:Chart"
                        width = 45.vw
                        height = 80.perc
                        marginTop = 6.vw
                        marginLeft = 5.vw
                        background = Background(Color.name(Col.ALICEBLUE))
                    }, grow = 1
                )

                add(
                    ChartLabelTable(
                        itemType,
                        itemSInfoList.recordList,
                        "description",
                        "color",
                        "calls",
                        label
                    ).apply {
                        id = "TheSimpleViewChartLabelTable:ChartLabelTable"
                        height = 97.perc
                        width = 40.vw
                        margin = 1.vw
                        background = Background(Color.name(Col.LIGHTPINK))
                    }, grow = 1
                )
            }
        }
    }
}
