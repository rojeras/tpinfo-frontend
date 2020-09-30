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
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.table.TableType
import pl.treksoft.kvision.tabulator.*
import pl.treksoft.kvision.utils.perc
import se.skoview.app.store
import se.skoview.common.HippoAction
import se.skoview.common.ItemType
import se.skoview.common.ViewMode
import se.skoview.common.isItemSelected

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
                itemSelectDeselect(itemId, itemType)
                "" // This lambda returns Any, which mean the last line must be an expression
            }
        )
    )
}


open class ChartLabelTable(
    itemType: ItemType,
    itemSInfoList: List<SInfoRecord>,
    dataField: String = "description",
    colorField: String = "color",
    callsField: String = "calls",
    //formatterField: String = "formatter",
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
                //height = "calc(100vh - 300px)",
                //height = "100%",
                //height = 100.vh,
                paginationSize = 1000,
                paginationButtonCount = 0,
                selectable = true,
                columns = listOf(
                    //firstCol,
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
                        //topCalc = Calc.COUNT,
                        topCalcFormatter = Formatter.COLOR,
                        headerFilter = Editor.INPUT,
                        headerFilterPlaceholder = "Sök ${heading.toLowerCase()}",
                        //headerFilterPlaceholder = "Sök...",
                        editable = { false },
                        //width = "20.vw",
                        widthGrow = 3,
                        //formatter = Formatter.TEXTAREA,
                        formatterComponentFunction = { cell, _, item ->
                            val itemRecord = item as SInfoRecord
                            Div {
                                if (store.getState().isItemSelected(itemRecord.itemType, itemRecord.itemId)) {
                                    //background = Background(Color.name(Col.LIGHTPINK))
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
                    val item = row.getData() as SInfoRecord
                    if (item.calls > -1) itemSelectDeselect(item.itemId, item.itemType)
                },
                // todo: Hide the tabulator footer here
                //dataLoaded = setStyle( ".tabulator-footer")
            )
        )
            .apply {
                //height = 50.vh
                height = 100.perc
            }
    }
}

private fun itemSelectDeselect(itemId: Int, itemType: ItemType) {
    println("In itemSelectDeselect()")
    //store.dispatch(HippoAction.PreSelectedLabelSet("default"))
    if (store.getState().isItemSelected(itemType, itemId)) {
        // De-select of an item
        // If we deselect an item which is part of the current PreSelect, then restore the default view
        //if (store.getState().preSelect!!.selectedItemsMap[itemType]!!.contains(itemId))
        //selectPreSelect("default")
        //else {
            store.dispatch(HippoAction.ItemIdDeselected(itemType, itemId))
            loadStatistics(store.getState())
        //}
//        store.dispatch(HippoAction.ItemIdDeselectedAll(itemType))
    } else {
        // Select an item
        if (store.getState().viewMode == ViewMode.SIMPLE) store.dispatch(HippoAction.SetViewMode(ViewMode.ADVANCED))
        store.dispatch(HippoAction.ItemIdSelected(itemType, itemId))
        loadStatistics(store.getState())
    }
}




