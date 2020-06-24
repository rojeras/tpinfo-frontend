package se.skoview.view

import com.github.snabbdom._get
import pl.treksoft.kvision.chart.*
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.core.Position
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
import se.skoview.data.*
import se.skoview.lib.thousands
import se.skoview.lib.getVersion

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

        // Date selector
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
                                    store.dispatch { dispatch, getState ->
                                        dispatch(HippoAction.DateSelected(DateType.EFFECTIVE, self.value ?: ""))
                                        loadStatistics(getState())
                                    }
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
                                //value = selectedPlattformChain!!.name
                                value = selectedPlattformId
                            ) {
                                addCssStyle(formControlXs)
                                background = Background(Color.name(Col.WHITE))
                            }.onEvent {
                                change = {
                                    val selectedTp = (self.value ?: "").toInt()
                                    val pChainId =
                                        PlattformChain.calculateId(first = selectedTp, middle = null, last = selectedTp)
                                    store.dispatch(HippoAction.ItemDeselectedAllForAllTypes)
                                    store.dispatch { dispatch, getState ->
                                        dispatch(
                                            HippoAction.ItemIdSelected(
                                                ItemType.PLATTFORM_CHAIN,
                                                //pChain!!
                                                pChainId
                                            )
                                        )
                                        loadStatistics(getState())
                                    }
                                }
                            }
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
                                    store.dispatch { dispatch, getState ->
                                        dispatch(HippoAction.DateSelected(DateType.END, self.value ?: ""))
                                        loadStatistics(getState())
                                    }
                                }
                            }
                        }
                    }
                }
                val calls = state.callsDomain.map { it.value }.sum()
                val tCalls = calls.toString().thousands()
                span { +"Totalt antal anrop för detta urval är: $tCalls" }.apply { align = Align.CENTER }
            }

            // About button
            div {
                //background = Background(Col.LIGHTSKYBLUE)
                align = Align.RIGHT
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
                    Chart(getChartConfig(ItemType.CONSUMER, SInfo.consumerSInfoList, animationTime = animateTime))
                add(consumerChartX).apply { width = 100.perc }
                add(
                    ChartLabelTable(
                        ItemType.CONSUMER,
                        SInfo.consumerSInfoList.recordList,
                        "description",
                        "color",
                        "calls",
                        "Tjänstekonsumenter"
                    )
                )

            }.apply {
                width = 24.vw
                margin = (0.3).vw
                //background = Background(Color.name(Col.BEIGE))
            }

            simplePanel {
                val contractChartX =
                    Chart(getChartConfig(ItemType.CONTRACT, SInfo.contractSInfoList, animationTime = animateTime))
                add(contractChartX)
                add(
                    ChartLabelTable(
                        ItemType.CONTRACT,
                        SInfo.contractSInfoList.recordList,
                        "description",
                        "color",
                        "calls",
                        "Tjänstekontrakt"
                    )
                )
            }.apply {
                width = 24.vw
                margin = (0.3).vw
            }

            simplePanel {
                val producerChartX =
                    Chart(getChartConfig(ItemType.PRODUCER, SInfo.producerSInfoList, animationTime = animateTime))
                add(producerChartX)
                add(
                    ChartLabelTable(
                        ItemType.PRODUCER,
                        SInfo.producerSInfoList.recordList,
                        "description",
                        "color",
                        "calls",
                        "Tjänsteproducenter"
                    )
                )
            }.apply {
                width = 24.vw
                margin = (0.3).vw
            }
            simplePanel {
                val logicalAddressChartX = Chart(
                    getChartConfig(
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
                        "Logiska adresser"
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
    //itemSInfoList: ObservableList<SInfoRecord>,
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
                    title = "",
                    formatter = Formatter.BUTTONCROSS
                )
            else
                ColumnDefinition<Any>(
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
                        title = heading,
                        field = dataField,
                        topCalc = Calc.COUNT,
                        topCalcFormatter = Formatter.COLOR,
                        headerFilter = Editor.INPUT,
                        //cellClick = {e, cell -> console.log(cell.getRow()) },
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
                    console.log(row)
                    val item = row.getData() as SInfoRecord
                    if (item.calls > -1) {
                        if (store.getState().isItemSelected(item.itemType, item.itemId)) {
                            store.dispatch { _, getState ->
                                store.dispatch(HippoAction.ItemIdDeselectedAll(itemType))
                                loadStatistics(getState())
                            }
                        } else {
                            store.dispatch { _, getState ->
                                store.dispatch(HippoAction.ItemIdSelected(itemType, item.itemId))
                                loadStatistics(getState())
                            }
                        }
                    }
                }
            ),
            types = setOf(TableType.BORDERED, TableType.STRIPED, TableType.HOVER)//,
            //background = Background(Col.BLUE)
        )
    }
}

private fun getChartConfig(
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
            animation = AnimationOptions(duration = animationTime),
            //responsive = false,
            legend = LegendOptions(display = false),
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
