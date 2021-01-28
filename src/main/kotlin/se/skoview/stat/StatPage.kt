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
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.utils.vh
import se.skoview.app.showBackgroundColorsForDebug
import se.skoview.common.HippoState

object StatPanelSize {
    val statHeaderSize: CssSize = 201.px // 200.99
    val chartPanelSize: CssSize = 24.vh
    val tablePanelCssSize = "calc(100vh - ${chartPanelSize.first}${chartPanelSize.second} - ${statHeaderSize.first}${statHeaderSize.second} - 26px)"
}

val statPageTop: Div = Div()

fun Container.statPage(
    state: HippoState,
) {
    println("TEST of size: ${StatPanelSize.tablePanelCssSize}")
    vPanel {
        if (showBackgroundColorsForDebug) background = Background(Color.name(Col.LIGHTBLUE))
        statHeader(state)
        statFourView(state)
    }
}
