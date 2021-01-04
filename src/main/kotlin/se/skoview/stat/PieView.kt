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
import pl.treksoft.kvision.panel.hPanel
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.vw
import se.skoview.common.HippoAction
import se.skoview.common.HippoState
import se.skoview.common.ItemType
import se.skoview.common.getHeightToRemainingViewPort

fun Container.pieView(state: HippoState) {
    div {
        // The whole item table
        hPanel(
            spacing = 1
        ) {
            overflow = Overflow.HIDDEN
            // background = Background(Color.name(Col.YELLOW))
            setStyle("height", getHeightToRemainingViewPort(statPageTop, 50))
            // height = 100.perc
            SInfo.createStatViewData(state)

            val animateTime =
                if (state.currentAction == HippoAction.DoneDownloadStatistics::class) {
                    1299
                } else {
                    -1
                }

            var numberOfColumns: Int = 0
            if (state.showConsumers) numberOfColumns++
            if (state.showProducers) numberOfColumns++
            if (state.showContracts) numberOfColumns++
            if (state.showLogicalAddresses) numberOfColumns++

            if (numberOfColumns < 1) return@hPanel

            if (state.showConsumers)
                statPieTableView(
                    state,
                    itemType = ItemType.CONSUMER,
                    itemSInfoList = SInfo.consumerSInfoList,
                    animateTime = animateTime,
                    label = getHeading(state, ItemType.CONSUMER),
                    numberOfColumns
                )

            if (state.showContracts)
                statPieTableView(
                    state,
                    itemType = ItemType.CONTRACT,
                    itemSInfoList = SInfo.contractSInfoList,
                    animateTime = animateTime,
                    label = getHeading(state, ItemType.CONTRACT),
                    numberOfColumns
                )

            if (state.showProducers)
                statPieTableView(
                    state,
                    itemType = ItemType.PRODUCER,
                    itemSInfoList = SInfo.producerSInfoList,
                    animateTime = animateTime,
                    label = getHeading(state, ItemType.PRODUCER),
                    numberOfColumns
                )

            if (state.showLogicalAddresses)
                statPieTableView(
                    state,
                    itemType = ItemType.LOGICAL_ADDRESS,
                    itemSInfoList = SInfo.logicalAddressSInfoList,
                    animateTime = animateTime,
                    label = getHeading(state, ItemType.LOGICAL_ADDRESS),
                    numberOfColumns
                )
        }
    }
}

fun Container.statPieTableView(
    state: HippoState,
    itemType: ItemType,
    itemSInfoList: SInfoList,
    animateTime: Int,
    label: String,
    numberOfColumns: Int
) {
    if (numberOfColumns < 1) return
    val columnWidth = (100 / numberOfColumns).toInt().vw

    val pieChart =
        Chart(
            getPieChartConfig(
                state,
                itemType,
                itemSInfoList,
                animationTime = animateTime,
                responsive = true,
                maintainAspectRatio = false
            )
        )

    val chartLabelTable =
        ChartLabelTable(
            state,
            itemType,
            itemSInfoList.recordList,
            "description",
            "color",
            "calls",
            label
        )

    if (numberOfColumns == 1) {
        // Show one pie and the table at the side
        flexPanel() {
            id = "TheSimpleViewBigPanel:FlexPanel"
            // overflow = Overflow.HIDDEN
            // height = 100.perc
            width = columnWidth

            setStyle("height", getHeightToRemainingViewPort(statPageTop, 40))

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
                chartLabelTable.apply {
                    id = "TheSimpleViewChartLabelTable:ChartLabelTable"
                    height = 97.perc
                    width = 40.vw
                    margin = 1.vw
                },
                grow = 1
            )
        }
    } else {
        // Show more than one pie, old "advanced mode"
        div {
            setStyle("height", getHeightToRemainingViewPort(statPageTop, 50))

            width = columnWidth

            add(
                pieChart.apply {
                    height = 30.perc
                }
            )
            add(
                chartLabelTable.apply { height = 70.perc }
            )
        }
    }
}

fun getHeading(state: HippoState, itemType: ItemType): String {
    if (state.showTechnicalTerms)
        return when (itemType) {
            ItemType.CONSUMER -> "Tjänstekonsumenter"
            ItemType.PRODUCER -> "Tjänsteproducenter"
            ItemType.CONTRACT -> "Tjänstekontrakt"
            ItemType.LOGICAL_ADDRESS -> "Logiska adresser"
            else -> "Internt fel i getHeading() - 1"
        }
    else { // ! state.showTechnicalTerms
        if (state.advancedViewPreSelect != null) {
            return state.advancedViewPreSelect.headingsMap[itemType]!!
        } else { // state.preSelect == null, specify defaults
            return when (itemType) {
                ItemType.CONSUMER -> "Applikationer"
                ItemType.PRODUCER -> "Informationskällor"
                ItemType.CONTRACT -> "Tjänster"
                ItemType.LOGICAL_ADDRESS -> "Adresser"
                else -> "Internt fel i getHeading() - 2"
            }
        }
    }
}
