
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
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.div
import pl.treksoft.kvision.panel.hPanel
import pl.treksoft.kvision.utils.*
import se.skoview.app.showBackgroundColorsForDebug
import se.skoview.common.*

fun Container.showPieCharts(
    state: HippoState,
) {
    hPanel(
        spacing = 1
    ) {
        id = "showPieCharts"
        if (showBackgroundColorsForDebug) background = Background(Color.name(Col.BLUE))
        // overflow = Overflow.HIDDEN
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
        ) 1299
        else -1

        val numberOfColumns = state.numberOfItemViewsSelected()

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

fun Container.piePanel(
    state: HippoState,
    itemType: ItemType,
    itemSInfoList: SInfoList,
    animateTime: Int,
    numberOfColumns: Int
) {
    if (numberOfColumns < 1) return

    val columnWidth =
        if (numberOfColumns > 1) (99.7 / numberOfColumns).vw
        else 49.vw

    val pieHight =
        if (numberOfColumns > 1) StatPanelSize.chartPanelHeight.asString()
        else StatPanelSize.singlePiePanelCssHeight

    val topMargin =
        if (numberOfColumns > 1) 0.px
        else 50.px

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

    div {
        id = "piePanel:$itemType"
        if (showBackgroundColorsForDebug) background = Background(Color.name(Col.LIGHTGRAY))
        setStyle("height", pieHight)
        marginTop = topMargin
        width = columnWidth

        if (!state.showTimeGraph)
            add(
                pieChart.apply {
                    height = 95.perc
                    width = 95.perc
                    marginBottom = 10.px
                    // height = StatPanelSize.chartPanelSize
                    // height = columnWidth - 2
                }
            )
    }
}

fun getPieChartConfig(
    itemType: ItemType,
    itemSInfoList: SInfoList,
    animationTime: Int = 0,
    responsive: Boolean = false,
    maintainAspectRatio: Boolean = true
): Configuration {
    return Configuration(
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
            responsive = responsive,
            legend = LegendOptions(display = false),
            maintainAspectRatio = maintainAspectRatio,
            onClick = { _, activeElements ->
                val sliceIx = activeElements[0]._get("_index") as Int
                val itemId: Int = itemSInfoList.recordList[sliceIx].itemId
                HippoManager.itemSelectDeselect(itemId, itemType)
                "" // This lambda returns Any, which mean the last line must be an expression
            }
        )
    )
}
