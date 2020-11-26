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
import pl.treksoft.kvision.core.Container
import pl.treksoft.kvision.core.Overflow
import pl.treksoft.kvision.html.div
import pl.treksoft.kvision.panel.flexPanel
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.vw
import se.skoview.common.HippoAction
import se.skoview.common.HippoState
import se.skoview.common.ItemType
import se.skoview.common.getHeightToRemainingViewPort

// Below is the code to show the different graphs for the simple version
fun Container.statSimpleView(state: HippoState) {
    div {
        println("In the simple view")
        id = "TheSimpleView:SimplePanel"

        id = "TheSimpleView:SimplePanel-Bind"
        height = 100.perc

        flexPanel() {
            id = "TheSimpleViewBigPanel:FlexPanel"
            overflow = Overflow.HIDDEN
            width = 100.vw

            setStyle("height", getHeightToRemainingViewPort(statPageTop, 40))

            height = 100.perc

            SInfo.createStatViewData(state)

            val animateTime =
                if (state.currentAction == HippoAction.DoneDownloadStatistics::class) {
                    1299
                } else {
                    -1
                }
            val preSelect: SimpleViewPreSelect = state.simpleViewPreSelect
            val showItemType: ItemType = preSelect.simpleModeViewOrder[0]

            val itemSInfoList: SInfoList
            val label: String = state.simpleViewPreSelect.label

            when (showItemType) {
                ItemType.CONSUMER -> {
                    itemSInfoList = SInfo.consumerSInfoList
                }
                ItemType.CONTRACT -> {
                    itemSInfoList = SInfo.contractSInfoList
                }
                ItemType.PRODUCER -> {
                    itemSInfoList = SInfo.producerSInfoList
                }
                ItemType.LOGICAL_ADDRESS -> {
                    itemSInfoList = SInfo.logicalAddressSInfoList
                }
                else -> {
                    println("ERROR in TheSimpleView, itemType = $showItemType")
                    itemSInfoList = SInfo.consumerSInfoList
                }
            }

            // width = 100.vw
            val pieChart =
                Chart(
                    getPieChartConfig(
                        state,
                        showItemType,
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
                },
                grow = 1
            )

            add(
                ChartLabelTable(
                    state,
                    showItemType,
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
                },
                grow = 1
            )
        }
    }
}
