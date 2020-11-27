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
import pl.treksoft.kvision.chart.* // ktlint-disable no-wildcard-imports
import pl.treksoft.kvision.core.* // ktlint-disable no-wildcard-imports
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.table.TableType
import pl.treksoft.kvision.tabulator.* // ktlint-disable no-wildcard-imports
import pl.treksoft.kvision.utils.perc
import se.skoview.common.* // ktlint-disable no-wildcard-imports

fun getPieChartConfig(
    state: HippoState,
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
                itemSelectDeselect(state, itemId, itemType)
                "" // This lambda returns Any, which mean the last line must be an expression
            }
        )
    )
}

open class ChartLabelTable(
    state: HippoState,
    itemType: ItemType,
    itemSInfoList: List<SInfoRecord>,
    dataField: String = "description",
    colorField: String = "color",
    callsField: String = "calls",
    heading: String
) : SimplePanel() {
    init {
        id = "ChartLabelTable: SimpleTable"
        // Footer pagination buttons hidden through CSS
        tabulator(
            data = itemSInfoList,
            types = setOf(TableType.BORDERED, TableType.STRIPED, TableType.HOVER),
            options = TabulatorOptions(
                layout = Layout.FITCOLUMNS,
                pagination = PaginationMode.LOCAL,
                paginationSize = 1000,
                paginationButtonCount = 0,
                selectable = true,
                columns = listOf(
                    ColumnDefinition<Any>(
                        headerSort = false,
                        title = "",
                        field = colorField,
                        width = "(0.3).px",
                        formatter = Formatter.COLOR
                    ),
                    ColumnDefinition(
                        headerSort = false,
                        title = "$heading (${itemSInfoList.size})",
                        field = dataField,
                        topCalcFormatter = Formatter.COLOR,
                        headerFilter = Editor.INPUT,
                        headerFilterPlaceholder = "Sök ${heading.toLowerCase()}",
                        editable = { false },
                        widthGrow = 3,
                        formatterComponentFunction = { cell, _, item ->
                            val itemRecord = item.unsafeCast<SInfoRecord>()
                            Div {
                                if (state.isItemSelected(itemRecord.itemType, itemRecord.itemId)) {
                                    fontWeight = FontWeight.BOLD
                                    cell.apply { background = Background(Color.name(Col.YELLOW)) }
                                }
                                whiteSpace = WhiteSpace.PREWRAP
                                wordBreak = WordBreak.BREAKALL
                                +itemRecord.description
                            }
                        }

                    ),
                    ColumnDefinition(
                        widthGrow = 1,
                        title = "Antal anrop",
                        hozAlign = Align.RIGHT,
                        field = callsField
                    )
                ),
                rowSelected = { row ->
                    val item = row.getData().unsafeCast<SInfoRecord>()
                    if (item.calls > -1) itemSelectDeselect(state, item.itemId, item.itemType)
                },
                // todo: Hide the tabulator footer here
            )
        )
            .apply {
                height = 100.perc
            }
    }
}

private fun itemSelectDeselect(state: HippoState, itemId: Int, itemType: ItemType) {
    println("In itemSelectDeselect()")
    if (state.isItemSelected(itemType, itemId)) {
        HippoManager.itemDeselected(itemId, itemType)
    } else {
        // Select an item
        if (state.view == View.STAT_SIMPLE)
            HippoManager.setView(View.STAT_ADVANCED)
        HippoManager.itemSelected(itemId, itemType)
    }
}
