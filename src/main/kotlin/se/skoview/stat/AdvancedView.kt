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
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.panel.hPanel
import pl.treksoft.kvision.state.bind
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.vw
import se.skoview.app.store
import se.skoview.common.HippoAction
import se.skoview.common.ItemType
import se.skoview.common.getHeightToRemainingViewPort

object AdvancedView : VPanel(
) {
    init {

        // The whole item table
        hPanel(
            spacing = 1
        ) {
        }.bind(store) { state ->
            overflow = Overflow.HIDDEN
            background = Background(Color.name(Col.YELLOW))
            setStyle("height", getHeightToRemainingViewPort(statPageTop, 50))
            //height = 100.perc
            SInfo.createStatViewData(state)

            val animateTime =
                if (state.currentAction == HippoAction.DoneDownloadStatistics::class) {
                    1299
                } else {
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
        background = Background(Color.name(Col.GREEN))
        setStyle("height", getHeightToRemainingViewPort(statPageTop, 50))
        //height = 100.perc
        width = 25.vw
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
        add(pieChart
            .apply {
                height = 30.perc
                background = Background(Color.name(Col.ALICEBLUE))
            })
        add(
            ChartLabelTable(
                itemType,
                itemSInfoList.recordList,
                "description",
                "color",
                "calls",
                label
            ).apply {
                height = 70.perc
            }
        )
    }
}