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
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.core.FlexWrap
import pl.treksoft.kvision.core.Position
import pl.treksoft.kvision.form.check.*
import pl.treksoft.kvision.form.select.simpleSelectInput
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.html.Align
import pl.treksoft.kvision.modal.Modal
import pl.treksoft.kvision.modal.ModalSize
import pl.treksoft.kvision.panel.*
import pl.treksoft.kvision.state.*
import pl.treksoft.kvision.table.TableType
import pl.treksoft.kvision.table.cell
import pl.treksoft.kvision.table.row
import pl.treksoft.kvision.table.table
import pl.treksoft.kvision.tabulator.*
import pl.treksoft.kvision.utils.*
import se.skoview.app.store
import se.skoview.common.*
import se.skoview.common.thousands
import se.skoview.common.getVersion
import se.skoview.app.formControlXs

// todo: Try to move this class inside the StatPage class

object StatPage : SimplePanel() {

    init {
        println("In CharTab():init()")

        this.marginTop = 10.px

        // Page header
        div {
            h2("Antal meddelanden genom SLL:s regionala tjänsteplattform")
            div("Detaljerad statistik med diagram och möjlighet att ladda ner informationen för egna analyser.")
        }.apply {
            //width = 100.perc
            background = Background(Color.hex(0x113d3d))
            align = Align.CENTER
            color = Color.name(Col.WHITE)
            marginTop = 5.px
        }

        flexPanel(
            FlexDir.ROW, FlexWrap.WRAP, FlexJustify.SPACEBETWEEN, FlexAlignItems.CENTER,
            spacing = 5
        ) {
            clear = Clear.BOTH
            margin = 0.px
            background = Background(Color.hex(0xf6efe9))
            div {
                align = Align.LEFT
            }.bind(store) { state ->
                println("After bind in header")
                table(
                    listOf(),
                    setOf(TableType.BORDERED, TableType.SMALL)
                ) {
                    // Star tdate
                    row {
                        cell { +"Startdatum:" }
                        cell {
                            simpleSelectInput(
                                options = state.statisticsDates.sortedByDescending { it }.map { Pair(it, it) },
                                value = state.dateEffective
                            ) {
                                addCssStyle(formControlXs)
                                background = Background(Color.name(Col.WHITE))
                            }.onEvent {
                                change = {
                                    //store.dispatch { dispatch, getState ->
                                    store.dispatch(HippoAction.DateSelected(DateType.EFFECTIVE, self.value ?: ""))
                                    loadStatistics(store.getState())
                                    //}
                                }
                            }
                        }
                        cell { +"Plattform:" }
                        cell {
                            val selectedPlattformId =
                                if (state.selectedPlattformChains.size > 0)
                                    PlattformChain.map[state.selectedPlattformChains[0]]!!.last.toString()
                                else ""
                            simpleSelectInput(
                                options = state.statisticsPlattforms.map { Pair(it.key.toString(), it.value.name) },
                                value = selectedPlattformId
                            ) {
                                addCssStyle(formControlXs)
                                background = Background(Color.name(Col.WHITE))
                            }.onEvent {
                                change = {
                                    val selectedTp = (self.value ?: "").toInt()
                                    val pChainId =
                                        PlattformChain.calculateId(first = selectedTp, middle = null, last = selectedTp)
                                    store.dispatch(HippoAction.ItemIdDeselectedAll(ItemType.PLATTFORM_CHAIN))
                                    store.dispatch(HippoAction.ItemDeselectedAllForAllTypes)
                                    //store.dispatch { dispatch, getState ->
                                    store.dispatch(
                                        HippoAction.ItemIdSelected(
                                            ItemType.PLATTFORM_CHAIN,
                                            pChainId
                                        )
                                    )
                                    loadStatistics(store.getState())
                                    //}
                                }
                            }
                        }

                        // Show time graph
                        cell {
                            checkBoxInput(
                                value = state.showTimeGraph
                            ).onClick {
                                if (value) loadHistory(state)
                                store.dispatch(HippoAction.ShowTimeGraph(value))
                            }
                            +" Visa utveckling över tid"
                        }
                    }
                    // End date
                    row {
                        cell { +"Slutdatum:" }
                        cell {
                            simpleSelectInput(
                                options = state.statisticsDates.sortedByDescending { it }.map { Pair(it, it) },
                                value = state.dateEnd
                            ) {
                                addCssStyle(formControlXs)
                                background = Background(Color.name(Col.WHITE))
                            }.onEvent {
                                change = {
                                    //store.dispatch { dispatch, getState ->
                                    store.dispatch(HippoAction.DateSelected(DateType.END, self.value ?: ""))
                                    loadStatistics(store.getState())
                                    //}
                                }
                            }
                        }

                        cell { +"Visa:" }
                        cell {
                            val selectedPreSelect = state.statPreSelect
                            simpleSelectInput(
                                options = StatPreSelect.selfStore.map { Pair(it.key, it.key) },
                                value = selectedPreSelect
                            ) {
                                addCssStyle(formControlXs)
                                background = Background(Color.name(Col.WHITE))
                            }.onEvent {
                                change = {
                                    val selectedPreSelect: String = self.value ?: "-"
                                    println("Selected pre-select: '$selectedPreSelect'")
                                    store.dispatch(HippoAction.PreSelectedSelected(selectedPreSelect))
                                    if (state.showTechnicalTerms) { // Restore technical labels
                                        store.dispatch(HippoAction.ShowTechnicalTerms(state.showTechnicalTerms))
                                    }
                                    store.dispatch(HippoAction.ItemDeselectedAllForAllTypes)
                                    val selectedItemsMap = StatPreSelect.selfStore[selectedPreSelect]!!.selectedItemsMap
                                    for ((itemType, itemIdList) in selectedItemsMap) {
                                        itemIdList.forEach {
                                            store.dispatch(HippoAction.ItemIdSelected(itemType, it))
                                        }
                                    }
                                    loadStatistics(store.getState())
                                }
                            }
                        }
                        cell {
                            checkBoxInput(
                                value = state.showTechnicalTerms
                            ).onClick {
                                //if (value) loadHistory(state)
                                println("In showTechnicalTerms, value = $value")
                                store.dispatch(HippoAction.ShowTechnicalTerms(value))
                                if (!value) { // Restore labels for current preselect
                                    store.dispatch(HippoAction.PreSelectedSelected(state.statPreSelect))
                                }
                            }
                            +" Tekniska termer"
                        }

                    }
                }
                val calls = state.statBlob.callsDomain.map { it.value }.sum()
                val tCalls = calls.toString().thousands()
                span { +"Totalt antal anrop för detta urval är: $tCalls" }.apply { align = Align.CENTER }
            }

            // About button
            vPanel {
                button("Exportera").onClick {
                    exportStatData(store.getState())
                }.apply {
                    addBsBgColor(BsBgColor.LIGHT)
                    addBsColor(BsColor.BLACK50)
                    marginBottom = 5.px
                }
                //background = Background(Col.LIGHTSKYBLUE)
                //align = Align.RIGHT
                val modal = Modal("Om Statistikfunktionen")
                modal.iframe(src = "about.html", iframeHeight = 400, iframeWidth = 700)
                modal.size = ModalSize.LARGE
                //modal.add(H(require("img/dog.jpg")))
                modal.addButton(Button("Stäng").onClick {
                    modal.hide()
                })
                button("Om Statistik ${getVersion("hippoVersion")}", style = ButtonStyle.INFO).onClick {
                    size = ButtonSize.SMALL
                    modal.show()
                }.apply {
                    addBsBgColor(BsBgColor.LIGHT)
                    addBsColor(BsColor.BLACK50)
                }

            }
        }

        // Below is the code to show the different graphs for the advanced version
        simplePanel { }.bind(store) { state ->
            if (state.showTimeGraph && state.historyMap.isNotEmpty()) {
                val animateTime =
                    if (state.currentAction == HippoAction.DoneDownloadHistory::class) {
                        1300
                    } else {
                        0
                    }

                println("Will display time graph")
                val xAxis = state.historyMap.keys.toList()
                val yAxis = state.historyMap.values.toList()
                chart(
                    Configuration(
                        ChartType.LINE,
                        listOf(
                            DataSets(
                                label = "Antal anrop per dag",
                                data = yAxis
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
                    height = 28.vh
                    width = 97.vw
                    //background = Background(Color.name(Col.AZURE))
                }
            }
        }

        // The whole item table
        hPanel() {
            //@Suppress("UnsafeCastFromDynamic")
            position = Position.ABSOLUTE
            width = 100.perc
            overflow = Overflow.AUTO
            background = Background(Color.hex(0xffffff))
        }.bind(store) { state ->

            //if (state.currentAction == HippoAction.DoneDownloadStatistics::class) {
            println("Time to update the view...")
            SInfo.createStatViewData(state)
            //}

            val animateTime =
                if (state.currentAction == HippoAction.DoneDownloadStatistics::class) {
                    //SInfo.createStatViewData(state)
                    println("Chart will now change")
                    1300
                } else {
                    println("Chart will NOT change")
                    0
                }

            simplePanel() {
                val consumerChartX =
                    Chart(
                        getPieChartConfig(
                            ItemType.CONSUMER,
                            SInfo.consumerSInfoList,
                            animationTime = animateTime
                        )
                    )
                add(consumerChartX).apply { width = 100.perc }
                add(
                    ChartLabelTable(
                        ItemType.CONSUMER,
                        SInfo.consumerSInfoList.recordList,
                        "description",
                        "color",
                        "calls",
                        state.consumerLabel
                    )
                )

            }.apply {
                width = 24.vw
                margin = (0.3).vw
                //background = Background(Color.name(Col.BEIGE))
            }

            simplePanel {
                val contractChartX =
                    Chart(
                        getPieChartConfig(
                            ItemType.CONTRACT,
                            SInfo.contractSInfoList,
                            animationTime = animateTime
                        )
                    )
                add(contractChartX)
                add(
                    ChartLabelTable(
                        ItemType.CONTRACT,
                        SInfo.contractSInfoList.recordList,
                        "description",
                        "color",
                        "calls",
                        state.contractLabel
                    )
                )
            }.apply {
                width = 24.vw
                margin = (0.3).vw
            }

            simplePanel {
                val producerChartX =
                    Chart(
                        getPieChartConfig(
                            ItemType.PRODUCER,
                            SInfo.producerSInfoList,
                            animationTime = animateTime
                        )
                    )
                add(producerChartX)
                add(
                    ChartLabelTable(
                        ItemType.PRODUCER,
                        SInfo.producerSInfoList.recordList,
                        "description",
                        "color",
                        "calls",
                        state.producerLabel
                    )
                )
            }.apply {
                width = 24.vw
                margin = (0.3).vw
            }
            simplePanel {
                val logicalAddressChartX = Chart(
                    getPieChartConfig(
                        ItemType.LOGICAL_ADDRESS,
                        SInfo.logicalAddressSInfoList,
                        animationTime = animateTime
                    )
                )
                add(logicalAddressChartX)
                add(
                    ChartLabelTable(
                        ItemType.LOGICAL_ADDRESS,
                        SInfo.logicalAddressSInfoList.recordList,
                        "description",
                        "color",
                        "calls",
                        state.laLabel
                    )
                )
            }.apply {
                width = 24.vw
                margin = (0.3).vw
            }
        }
    }
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
                        hozAlign = pl.treksoft.kvision.tabulator.Align.RIGHT,
                        field = callsField
                    )
                ),
                pagination = PaginationMode.LOCAL,
                //height = "611px",
                height = "50vh",
                paginationSize = 100,
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


