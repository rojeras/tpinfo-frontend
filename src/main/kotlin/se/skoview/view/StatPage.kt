package se.skoview.view

import com.github.snabbdom._get
import pl.treksoft.kvision.chart.*
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.core.Position
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
import se.skoview.lib.thousands
import se.skoview.lib.getColorForObject
import se.skoview.lib.getVersion

object StatPage : SimplePanel() {

    //private val filter: Filter = Filter()
    val consumerChart: Chart
    val producerChart: Chart
    val logicalAddressChart: Chart
    val contractChart: Chart

    var noCalls: Int = -1

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

    init {
        println("In CharTab():init()")

        this.marginTop = 10.px

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
                /*
                if (
                    state.currentAction != HippoAction.DoneDownloadStatistics::class //&&
                //state.currentAction != HippoAction.ViewUpdated::class //&&
                //state.currentAction != HippoAction.ItemIdSelected::class
                ) return@bind
                 */
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
                                PlattformChain.map[state.selectedPlattformChains[0]]!!.last.toString()
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
                                    store.dispatch(HippoAction.ItemIdDeselectedAll(ItemType.PLATTFORM_CHAIN))
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
            /*
            if (
                state.currentAction != HippoAction.DoneDownloadStatistics::class //&&
            //state.currentAction != HippoAction.ItemIdSelected::class
            ) return@bind
             */
            println("Time to update the view...")
            SInfo.view(state)
            simplePanel() {
                //add(consumerChart).apply { width = 100.perc }
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

            }.apply {
                width = 24.vw
                margin = (0.3).vw
                //background = Background(Color.name(Col.BEIGE))
            }

            simplePanel {
                //add(contractChart)
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
                //add(producerChart)
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
                //add(logicalAddressChart)
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
    itemSInfoList: ObservableList<SInfoRecord>,
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
                        align = pl.treksoft.kvision.tabulator.Align.RIGHT,
                        field = callsField

                        //width = "5.vw"
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

// ---------------------------------------------------------------------------------------------

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
        var desc: String = ""
        for (entry in ackMapTmp) {
            when (this.itemType) {
                ItemType.CONSUMER -> {
                    item = ServiceComponent.map[entry.key]
                    desc = "${item!!.description} (${item.hsaId})"
                }
                ItemType.PRODUCER -> {
                    item = ServiceComponent.map[entry.key]
                    desc = "${item!!.description} (${item.hsaId})"
                }
                ItemType.LOGICAL_ADDRESS -> {
                    item = LogicalAddress.map[entry.key]
                    desc = "${item!!.description} (${item.name})"
                }
                ItemType.CONTRACT -> {
                    item = ServiceContract.map[entry.key]
                    desc = item!!.description
                }
                else -> error("Unknown itemType in populate!")
            }
            //println("${item!!.description} has calls: ${ackMapTmp[entry.key]}")

            callsTmp.add(
                SInfoRecord(
                    this.itemType,
                    item!!.id,
                    desc,
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