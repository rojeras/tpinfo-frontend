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
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.div
import pl.treksoft.kvision.panel.hPanel
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.utils.*
import se.skoview.app.showBackgroundColorsForDebug
import se.skoview.common.HippoAction
import se.skoview.common.HippoState
import se.skoview.common.ItemType

fun Container.statFourView(state: HippoState) {
    vPanel {
        id = "statFourView"
        if (showBackgroundColorsForDebug) background = Background(Color.name(Col.LIGHTGREEN))
        // height = 20.vh
        if (state.showTimeGraph) showHistoryChart(state)
        else showPieCharts(state)

        showItemTables(state)
    }
}

fun Container.showPieCharts(
    state: HippoState,
) {
    hPanel(
        spacing = 1
    ) {
        id = "showPieCharts"
        if (showBackgroundColorsForDebug) background = Background(Color.name(Col.LIGHTCORAL))
        overflow = Overflow.HIDDEN
        /*
        if (state.showTimeGraph)
            setStyle("height", getHeightToRemainingViewPort(statPageTop, 300))
        else
            setStyle("height", getHeightToRemainingViewPort(statPageTop, 80))
        */
        // height = 100.perc
        SInfo.createStatViewData(state)

        val animateTime = if (
            state.currentAction == HippoAction.DoneDownloadStatistics::class // ||
            // state.currentAction == HippoAction.DoneDownloadHistory::class
        ) 1299
        else -1

        var numberOfColumns = 0
        if (state.showConsumers) numberOfColumns++
        if (state.showProducers) numberOfColumns++
        if (state.showContracts) numberOfColumns++
        if (state.showLogicalAddresses) numberOfColumns++

        if (numberOfColumns < 1) return@hPanel

        if (state.showConsumers)
            piePanel(
                state,
                itemType = ItemType.CONSUMER,
                itemSInfoList = SInfo.consumerSInfoList,
                animateTime = animateTime,
                numberOfColumns
            )

        if (state.showContracts)
            piePanel(
                state,
                itemType = ItemType.CONTRACT,
                itemSInfoList = SInfo.contractSInfoList,
                animateTime = animateTime,
                numberOfColumns
            )

        if (state.showProducers)
            piePanel(
                state,
                itemType = ItemType.PRODUCER,
                itemSInfoList = SInfo.producerSInfoList,
                animateTime = animateTime,
                numberOfColumns
            )

        if (state.showLogicalAddresses)
            piePanel(
                state,
                itemType = ItemType.LOGICAL_ADDRESS,
                itemSInfoList = SInfo.logicalAddressSInfoList,
                animateTime = animateTime,
                numberOfColumns
            )
    }
}

fun Container.showItemTables(
    state: HippoState,
) {
    hPanel(
        spacing = 1
    ) {
        id = "showItemTables"
        if (showBackgroundColorsForDebug) background = Background(Color.name(Col.YELLOW))
        // height = 50.vh
        setStyle("height", "calc(100vh - 24vh - 230px)")
        marginTop = 5.px
        overflow = Overflow.HIDDEN
        // background = Background(Color.name(Col.YELLOW))
        /*
        if (state.showTimeGraph)
            setStyle("height", getHeightToRemainingViewPort(statPageTop, 300))
        else
            setStyle("height", getHeightToRemainingViewPort(statPageTop, 80))
         */

        // height = 100.perc
        SInfo.createStatViewData(state)

        var numberOfColumns = 0
        if (state.showConsumers) numberOfColumns++
        if (state.showProducers) numberOfColumns++
        if (state.showContracts) numberOfColumns++
        if (state.showLogicalAddresses) numberOfColumns++

        if (numberOfColumns < 1) return@hPanel

        if (state.showConsumers)
            itemTablePanel(
                state,
                itemSInfoList = SInfo.consumerSInfoList,
                label = getHeading(state, ItemType.CONSUMER),
                numberOfColumns
            )

        if (state.showContracts)
            itemTablePanel(
                state,
                itemSInfoList = SInfo.contractSInfoList,
                label = getHeading(state, ItemType.CONTRACT),
                numberOfColumns
            )

        if (state.showProducers)
            itemTablePanel(
                state,
                itemSInfoList = SInfo.producerSInfoList,
                label = getHeading(state, ItemType.PRODUCER),
                numberOfColumns
            )

        if (state.showLogicalAddresses)
            itemTablePanel(
                state,
                itemSInfoList = SInfo.logicalAddressSInfoList,
                label = getHeading(state, ItemType.LOGICAL_ADDRESS),
                numberOfColumns
            )
    }
}

fun Container.piePanel(
    state: HippoState,
    itemType: ItemType,
    itemSInfoList: SInfoList,
    animateTime: Int,
    numberOfColumns: Int
) {
    if (numberOfColumns < 1) return
    val columnWidth = (100 / numberOfColumns).vw - 0.1

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

    div {
        id = "piePanel:$itemType"
        // setStyle("height", getHeightToRemainingViewPort(statPageTop, 50))

        width = columnWidth

        if (!state.showTimeGraph)
            add(
                pieChart.apply {
                    height = StatPanelSize.chartPanelSize
                    // height = 30.perc
                }
            )
    }
}

fun Container.itemTablePanel(
    state: HippoState,
    itemSInfoList: SInfoList,
    label: String,
    numberOfColumns: Int
) {
    if (numberOfColumns < 1) return
    val columnWidth = (100 / numberOfColumns).vw - 0.1

    val chartLabelTable =
        ChartLabelTable(
            state,
            itemSInfoList.recordList,
            "description",
            "color",
            "calls",
            label
        )

    div {
        id = "itemTablePanel:$label"
        if (showBackgroundColorsForDebug) background = Background(Color.name(Col.LIGHTSTEELBLUE))
        width = columnWidth
        add(chartLabelTable) // .apply { height = 70.perc }
    }
}
