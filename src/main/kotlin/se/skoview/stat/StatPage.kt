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

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.panel.hPanel
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.utils.asString
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.utils.vh
import pl.treksoft.kvision.utils.vw
import se.skoview.app.showBackgroundColorsForDebug
import se.skoview.common.HippoState
import se.skoview.common.ItemType
import se.skoview.common.numberOfItemViewsSelected

object StatPanelSize {
    var statHeaderHeightPx: Int = 100
    val statHeaderHeight: CssSize = 201.px // 200.99
    val chartPanelHeight: CssSize = 24.vh
    val singlePiePanelCssHeight: String = "calc(90vh - ${statHeaderHeight.asString()}"
    val tablePanelSingleCssHeight: String = "calc(92vh - ${statHeaderHeight.asString()})"
    val tablePanelMultipleCssHeight: String = "calc(95vh - ${statHeaderHeight.asString()} - ${chartPanelHeight.asString()})"
}

fun Container.statPage(
    state: HippoState,
) {
    vPanel {
        width = 99.vw
        overflow = Overflow.HIDDEN
        if (showBackgroundColorsForDebug) background = Background(Color.name(Col.LIGHTBLUE))
        statHeader(state)
        statMainView(state)
    }
}

fun Container.statMainView(state: HippoState) {
    if (state.showTimeGraph) {
        vPanel {
            showHistoryChart(state)
            showItemTables(state)
        }
    } else if (state.numberOfItemViewsSelected() == 1) {
        hPanel {
            if (showBackgroundColorsForDebug) background = Background(Color.name(Col.LIGHTSEAGREEN))
            width = 99.vw
            overflow = Overflow.HIDDEN
            setStyle("height", "calc(96vh - ${StatPanelSize.statHeaderHeight.asString()}")
            showPieCharts(state)
            showItemTables(state)
        }
    } else {
        vPanel {
            // width = 98.vw
            showPieCharts(state) // .apply { height = 30.perc }
            showItemTables(state) // .apply { height = 70.perc }
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
        if (state.viewPreSelect != null) {
            return state.viewPreSelect.headingsMap[itemType]!!
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
