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

import pl.treksoft.kvision.chart.*
import pl.treksoft.kvision.core.Container
import pl.treksoft.kvision.utils.vw
import se.skoview.common.HippoAction
import se.skoview.common.HippoState

// Time graph
fun Container.showHistoryChart(state: HippoState) {
    if (
        state.historyMap.isNotEmpty()
    ) {
        val animateTime =
            if (state.currentAction == HippoAction.DoneDownloadHistory::class) {
                1298
            } else {
                -2
            }

        val xAxis = state.historyMap.keys.toList()
        val yAxis = state.historyMap.values.toList()
        chart(
            Configuration(
                ChartType.LINE,
                listOf(
                    DataSets(
                        label = "Antal anrop per dag",
                        data = yAxis,
                        lineTension = 0
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
            id = "showHistoryChart"
            height = StatPanelSize.chartPanelHeight
            width = 99.vw
        }
    }
}
