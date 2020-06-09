package se.skoview.view

import com.github.snabbdom._get
import pl.treksoft.kvision.chart.*
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.data.BaseDataComponent
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
import se.skoview.lib.getColorForObject

object StatPage : SimplePanel() {

    //private val filter: Filter = Filter()

    val consumerChart: Chart
    val producerChart: Chart
    val logicalAddressChart: Chart
    val contractChart: Chart

    private fun getChartConfigConsumer(): Configuration {
        return getChartConfig(ItemType.CONSUMER, SInfo.consumerSInfoList)
    }

    private fun getChartConfigProducer(): Configuration {
        return getChartConfig(ItemType.PRODUCER, SInfo.producerSInfoList)
    }

    private fun getChartConfigLogicalAddress(): Configuration {
        return getChartConfig(ItemType.LOGICAL_ADDRESS, SInfo.logicalAddressSInfoList)
    }

    private fun getChartConfigContract(): Configuration {
        return getChartConfig(ItemType.CONTRACT, SInfo.contractSInfoList)
    }

    private fun getChartConfig(itemType: ItemType, itemSInfoList: SInfoList): Configuration {
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
                //responsive = false,
                legend = LegendOptions(display = false),
                onClick = { _, activeElements ->
                    val sliceIx = activeElements[0]._get("_index") as Int
                    val itemId: Int = itemSInfoList.recordList[sliceIx].itemId
                    //filter.toggle(itemType, itemId)
                    //StatisticsInfo.mkStatisticsInfo(filter) { SInfo.view(it) }
                    "" // This lambda returns Any, which mean the last line must be an expression
                }
            )
        )
        return configuration
    }

    init {
        println("In CharTab():init()")
        //getDatesLastMonth()
        // Default date range is "last month"
        /*
        val datePair = getDatesLastMonth()
        filter.setDate(type = ItemType.DATE_EFFECTIVE, date = datePair.first)
        filter.setDate(type = ItemType.DATE_END, date = datePair.second)
        filter.dump()
        //filter.dateEffective = datePair.first
        //filter.dateEnd = datePair.second
        val plattformId = 3
        filter.set(ItemType.PLATTFORM, plattformId)

        StatisticsInfo.mkStatisticsInfo(filter) { SInfo.view(it) }
        */
        //loadStatistics(store.getState())
        //SInfo.view(store.getState())

        this.marginTop = 10.px



        //vPanel {
        //gridPanel( templateColumns = "50% 50%", columnGap = 20, rowGap = 20 ) {
        /*
        div {
            h2("KStat - statistik baserad på SLL RTPs")
        }.apply {
            width = 100.perc
            background = Background(Col.DARKGREEN)
            align = Align.CENTER
            color = Color(Col.WHITE)
        }
        hPanel {
            span("Tjänsteplattform: ")
            add(SelectTp())
            span("Från datum: ")
            add(SEffectiveDateSelectPanel(StatisticsInfo.getSdateOtionsList()))
            span("Till datum: ")
            add(SEndDateSelectPanel(StatisticsInfo.getSdateOtionsList()))
        }.apply {
            width = 100.perc
            background = Background(Col.GRAY)
            //align = Align.CENTER
            color = Color(Col.WHITE)
        }
         */
        //StatTablePage.fontFamily = "Times New Roman"
        // Page header
        vPanel {
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
                //}.stateBinding(store) { state ->
            }.bind(store) { state ->
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
                            simpleSelectInput(
                                //options = listOf("RTP Prod", "RTP QA").map { Pair(it, it) },
                                options = listOf(Pair("3", "RTP Prod"), Pair("4", "RTP QA")),
                                value = state.dateEnd
                            ) {
                                addCssStyle(formControlXs)
                                background = Background(Color.name(Col.WHITE))
                            }.onEvent {
                                change = {
                                    val selectedTp = (self.value ?: "").toInt()
                                    val pChain =
                                        PlattformChain.calculateId(first = selectedTp, middle = null, last = selectedTp)
                                    store.dispatch { dispatch, getState ->
                                        dispatch(
                                            HippoAction.ItemSelected(
                                                ItemType.PLATTFORM_CHAIN,
                                                PlattformChain.map[pChain]!!
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
                button("Om Statistik", style = ButtonStyle.INFO).onClick {
                    size = ButtonSize.SMALL
                    modal.show()
                }.apply {
                    addBsBgColor(BsBgColor.LIGHT)
                    addBsColor(BsColor.BLACK50)
                }
            }
        }

        consumerChart = Chart(getChartConfigConsumer())
        producerChart = Chart(getChartConfigProducer())
        logicalAddressChart = Chart(getChartConfigLogicalAddress())
        contractChart = Chart(getChartConfigContract())

        SInfo.consumerSInfoList.recordList.onUpdate += {
            consumerChart.configuration = getChartConfigConsumer()
        }

        SInfo.producerSInfoList.recordList.onUpdate += {
            producerChart.configuration = getChartConfigProducer()
        }

        SInfo.logicalAddressSInfoList.recordList.onUpdate += {
            logicalAddressChart.configuration = getChartConfigLogicalAddress()
        }

        SInfo.contractSInfoList.recordList.onUpdate += {
            contractChart.configuration = getChartConfigContract()
        }

        hPanel() {

            @Suppress("UnsafeCastFromDynamic")

            div {
                align = Align.LEFT
                // }.stateBinding(store) { state ->
            }.bind(store) { state ->
                //hPanel {
                SInfo.view(state)
                vPanel() {

                    add(consumerChart)
                    println("consumerSInfoList:")
                    console.log(SInfo.consumerSInfoList)

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


                }
                /*
                vPanel {
                    add(contractChart)
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
                }
                vPanel {
                    add(logicalAddressChart)
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
                }

                vPanel {
                    add(producerChart)
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
                }
                */
                //}
            }
        }
    }
}

open class ChartLabelTable(
    itemType: ItemType,
    itemSInfoList: ObservableList<SInfoRecord>,
    dataField: String = "description",
    colorField: String = "color",
    callsField: String = "calls",
    heading: String
) : SimplePanel() {
    init {
        //background = Background(Col.BLUE)

        println("In ChartLabelTable")
        console.log(itemType)
        console.log(itemSInfoList)
        console.log(dataField)
        console.log(colorField)
        console.log(callsField)
        console.log(heading)

        tabulator(
            itemSInfoList,
            options = TabulatorOptions(
                //layout = Layout.FITDATAFILL,
                layout = Layout.FITCOLUMNS,
                columns = listOf(
                    ColumnDefinition(
                        title = "",
                        field = colorField,
                        formatter = Formatter.COLOR,
                        width = "10.px"
                    ),
                    ColumnDefinition(
                        heading,
                        dataField,
                        //topCalc = Calc.COUNT,
                        //topCalcFormatter = Formatter.COLOR,
                        headerFilter = Editor.INPUT,
                        //cellClick = {e, cell -> console.log(cell.getRow()) },
                        editable = { false },
                        width = "120.px",
                        formatterComponentFunction = { _, _, item ->
                            Div(item.description) {

                                /*
                                if (filter.isItemFiltered(item.itemType, item.itemId)) {
                                    background = Background(Col.LIGHTSTEELBLUE)
                                }
                                 */
                            }
                        }
                        //cellDblClick = { _, cell -> cell.edit(true) }
                    ),
                    ColumnDefinition(
                        title = "Antal anrop",
                        field = callsField,
                        width = "30.px"

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
                        store.dispatch(HippoAction.ItemSelected(viewType = itemType, ))
                        /*
                        val filterType = itemType  //ItemType.CONSUMER
                val actualType = item.itemType
                if (actualType == ItemType.DOMAIN) {
                    filterType = ItemType.DOMAIN
                }
                 */
                        //filter.toggle(filterType, item.itemId)
                        //val iI = StatisticsInfo.mkStatisticsInfo(filter) { SInfo.view(it) }
                        //console.log(iI)
                    }

                }
            ),
            types = setOf(TableType.BORDERED, TableType.STRIPED, TableType.HOVER)//,
            //background = Background(Col.BLUE)
        )
    }
}

// ---------------------------------------------------------------------------------------------

/**
 * The singleton for the data displayed in the view
 * It is updated through the view() method with an statisticsInfo object as parameter.
 */

object SInfo : BaseDataComponent() {
    var consumerSInfoList = SInfoList(ItemType.CONSUMER)
    var producerSInfoList = SInfoList(ItemType.PRODUCER)
    var logicalAddressSInfoList = SInfoList(ItemType.LOGICAL_ADDRESS)
    var contractSInfoList = SInfoList(ItemType.CONTRACT)

    fun view(state: HippoState) {
        println("In the SInfo.view()")
        consumerSInfoList.populate(state.callsConsumer)
        producerSInfoList.populate(state.callsProducer)
        logicalAddressSInfoList.populate(state.callsLogicalAddress)
        contractSInfoList.populate(state.callsContract)

    }
}

class SInfoRecord(
    val itemType: ItemType,
    val itemId: Int,
    val description: String,
    val calls: Int,
    val responseTime: Int
) {
    val color: Color

    init {
        // A random color is assigned to each object (based on hash value of description field)
        color = getColorForObject(description)
    }
}

class SInfoList(val itemType: ItemType) {

    val recordList: ObservableListWrapper<SInfoRecord> =
        observableListOf<SInfoRecord>() as ObservableListWrapper<SInfoRecord>

    fun callList(): List<Int> {
        return recordList.map { it.calls }
    }

    fun colorList(): List<Color> {
        return recordList.map { it.color }
    }

    fun descList(): List<String> {
        return recordList.map { it.description }
    }

    fun populate(ackMap: Map<Int, Int>) {
        // Need to go via a temp collection to stop a mass of GUI updates
        val ackMapTmp = ackMap.toList().sortedBy { (_, value) -> value }.reversed().toMap()
        val callsTmp = mutableListOf<SInfoRecord>()

        var item: BaseItem?
        for (entry in ackMapTmp) {
            item = when (this.itemType) {
                ItemType.CONSUMER -> ServiceComponent.map[entry.key]
                ItemType.PRODUCER -> ServiceComponent.map[entry.key]
                ItemType.LOGICAL_ADDRESS -> LogicalAddress.map[entry.key]
                ItemType.CONTRACT -> ServiceContract.map[entry.key]
                else -> error("Unknown itemType in populate!")
            }
            //println("${item!!.description} has calls: ${ackMapTmp[entry.key]}")
            callsTmp.add(
                SInfoRecord(
                    this.itemType,
                    item!!.id,
                    item.description,
                    ackMapTmp[entry.key] ?: error("ackMapTmp is null"),
                    0
                )
            )
        }

        // Sort the recordList in reverse order based on number of calls
        callsTmp.sortBy { it.calls }
        callsTmp.reverse()

        this.recordList.clear()
        this.recordList.addAll(callsTmp)
    }
}

