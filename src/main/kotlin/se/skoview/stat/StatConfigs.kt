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
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.table.TableType
import pl.treksoft.kvision.tabulator.*
import se.skoview.app.store
import se.skoview.common.HippoAction
import se.skoview.common.ItemType
import se.skoview.common.isItemSelected

fun getSimplePieChartConfig(
    itemType: ItemType,
    itemSInfoList: SInfoList,
    animationTime: Int = 0
): Configuration {
    val configuration = Configuration(
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
            responsive = true,
            legend = LegendOptions(display = false),
            maintainAspectRatio = false,
            onClick = { _, activeElements ->
                val sliceIx = activeElements[0]._get("_index") as Int
                val itemId: Int = itemSInfoList.recordList[sliceIx].itemId
                if (store.getState().isItemSelected(itemType, itemId)) {
                    store.dispatch { _, getState ->
                        store.dispatch(HippoAction.ItemIdDeselectedAll(itemType))
                        loadStatistics(getState())
                    }
                } else {
                    store.dispatch { _, getState ->
                        store.dispatch(HippoAction.ItemIdSelected(itemType, itemId))
                        loadStatistics(getState())
                    }

                }
                "" // This lambda returns Any, which mean the last line must be an expression
            }
        )
    )
    return configuration
}

fun getPieChartConfig(
    itemType: ItemType,
    itemSInfoList: SInfoList,
    animationTime: Int = 0,
    responsive: Boolean = false,
    maintainAspectRatio: Boolean = true
): Configuration {
    val configuration = Configuration(
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
                if (store.getState().isItemSelected(itemType, itemId)) {
                    store.dispatch { _, getState ->
                        store.dispatch(HippoAction.ItemIdDeselectedAll(itemType))
                        loadStatistics(getState())
                    }
                } else {
                    store.dispatch { _, getState ->
                        store.dispatch(HippoAction.ItemIdSelected(itemType, itemId))
                        loadStatistics(getState())
                    }

                }
                "" // This lambda returns Any, which mean the last line must be an expression
            }
        )
    )
    return configuration
}

fun getLineChartConfig(
    historyMap: Map<String, Int>,
    animationTime: Int = 0
): Configuration {
    val configuration = Configuration(
        ChartType.LINE,
        listOf(
            DataSets(
                label = "Antal anrop per dag",
                data = historyMap.values.toList()
            )
        ),
        historyMap.keys.toList(),
        options = ChartOptions(
            animation = AnimationOptions(duration = animationTime),
            //responsive = false
            maintainAspectRatio = false
        )
    )
    return configuration
}

open class ChartLabelTable(
    itemType: ItemType,
    itemSInfoList: List<SInfoRecord>,
    dataField: String = "description",
    colorField: String = "color",
    callsField: String = "calls",
    heading: String
) : SimplePanel() {
    init {
        // Color or red cross if item is selected
        val firstCol =
            if (
                itemSInfoList.size == 1 &&
                store.getState().isItemSelected(itemType, itemSInfoList[0].itemId)
            )
                ColumnDefinition(
                    headerSort = false,
                    title = "",
                    formatter = Formatter.BUTTONCROSS
                )
            else
                ColumnDefinition<Any>(
                    headerSort = false,
                    title = "",
                    field = colorField,
                    width = "(0.3).px",
                    formatter = Formatter.COLOR
                )

        tabulator(
            itemSInfoList,
            options = TabulatorOptions(
                layout = Layout.FITCOLUMNS,
                columns = listOf(
                    firstCol,
                    ColumnDefinition(
                        headerSort = false,
                        title = heading,
                        field = dataField,
                        topCalc = Calc.COUNT,
                        topCalcFormatter = Formatter.COLOR,
                        headerFilter = Editor.INPUT,
                        //headerFilterPlaceholder = "Sök ${heading.toLowerCase()}",
                        headerFilterPlaceholder = "Sök...",
                        editable = { false },
                        //width = "20.vw",
                        widthGrow = 3,
                        formatter = Formatter.TEXTAREA
                        /*
                        formatterComponentFunction = { _, _, item ->
                            Div(item.description) {
                                if (store.getState().isItemSelected(item.itemType, item.itemId)) {
                                    background = Background(Color.name(Col.LIGHTSTEELBLUE))
                                }
                            }
                        }
                         */
                        //cellDblClick = { _, cell -> cell.edit(true) }
                    ),
                    ColumnDefinition(
                        widthGrow = 1,
                        title = "Antal",
                        hozAlign = Align.RIGHT,
                        field = callsField
                    )
                ),
                //pagination = PaginationMode.LOCAL,
                //height = "611px",
                height = "50vh",
                paginationSize = 1000,
                //dataTree = true,
                selectable = true,
                rowSelected = { row ->
                    val item = row.getData() as SInfoRecord
                    if (item.calls > -1) {
                        if (store.getState().isItemSelected(item.itemType, item.itemId)) {
                            //store.dispatch { _, getState ->
                            store.dispatch(HippoAction.ItemIdDeselectedAll(itemType))
                            loadStatistics(store.getState())
                            //}
                        } else {
                            //store.dispatch { _, getState ->
                            store.dispatch(HippoAction.ItemIdSelected(itemType, item.itemId))
                            loadStatistics(store.getState())
                            //}
                        }
                    }
                }
            ),
            types = setOf(TableType.BORDERED, TableType.STRIPED, TableType.HOVER)//,
            //background = Background(Col.BLUE)
        )
    }
}
