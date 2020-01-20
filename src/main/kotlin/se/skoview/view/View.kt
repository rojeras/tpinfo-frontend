package se.skoview.view

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.form.InputSize
import pl.treksoft.kvision.form.select.selectInput
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.html.Align
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.state.stateBinding
import pl.treksoft.kvision.table.*
import pl.treksoft.kvision.tabulator.*
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px
import se.skoview.data.*
import kotlin.browser.window

data class ViewInformation(
    val baseItem: BaseItem,
    val showData: String,
    val type: ItemType
)

object hippoPage : SimplePanel() {

    init {

        println("In hippoPage")

        vPanel {
            div {
                h2("RHippo - integrationer via tjänsteplattform/ar för nationell e-hälsa")
                div("Integrationer för tjänsteplattformar vars tjänstadresseringskatalog (TAK) är tillgänglig i Ineras TAK-api visas.").apply {
                    width = 100.perc
                }
            }.apply {
                width = 100.perc
                background = Background(0x113d3d)
                align = Align.CENTER
                color = Color(Col.WHITE)
                marginTop = 5.px
            }
        }
        vPanel {
        }.apply {
            width = 100.perc
            clear = Clear.BOTH
            margin = 0.px
            background = Background(0xf6efe9)
        }.stateBinding(store) { state ->
            div(classes = setOf(""""class="cssload-loader""""))
            div {
                //add(DateSelectPanel(state.updateDates, state.dateEffective.toSwedishDate()))
                val dateOptionList = state.updateDates.map { Pair(it, it) }
                selectInput(
                    options = dateOptionList,
                    value = state.dateEffective
                )
                    .apply {
                        selectWidth = CssSize(150, UNIT.px)
                        size = InputSize.SMALL
                    }
                    .onEvent {
                    change = {
                        println("Date selected:")
                        console.log(self.value)
                        store.dispatch(HippoAction.DateSelected(self.value ?: ""))
                        store.dispatch { dispatch, getState ->
                            dispatch(HippoAction.DateSelected(self.value ?: ""))
                            loadIntegrations(getState())
                        }
                    }
                }
            }
        }
        vPanel {
        }.apply {
            //background = Background(0x009090)
            width = 100.perc
        }.stateBinding(store) { state ->



            //println("========================> Actions submitter: ${state::class.simpleName}")
            val consumerHeading: String =
                "Tjänstekonsumenter (${state.vServiceConsumers.size}/${state.maxCounters.consumers})"
            val contractHeading: String =
                "Tjänstekontrakt (${state.vServiceContracts.size}/${state.maxCounters.contracts})"
            val plattformHeading: String =
                "Tjänsteplattformar (${state.vPlattformChains.size}/${state.maxCounters.plattformChains})"
            val logicalAddressHeading: String =
                "Logiska adresser (${state.vLogicalAddresses.size}/${state.maxCounters.logicalAddress})"
            val producerHeading: String =
                "Tjänsteproducenter (${state.vServiceProducers.size}/${state.maxCounters.producers})"
            table(
                listOf(
                    "",
                    "",
                    "",
                    "",
                    ""
                ),
                setOf(TableType.SMALL, TableType.BORDERED),
                classes = setOf("table-layout:fixed")
            )
            {
                row(classes = setOf("table-layout:fixed")) {
                    // Service consumers
                    cell(classes = setOf("table-layout:fixed")) {
                        val viewConsumerLst: MutableList<ViewInformation> = mutableListOf()
                        state.vServiceConsumers.map {
                            viewConsumerLst.add(
                                ViewInformation(
                                    it,
                                    "<i>${it.description}</i><br>${it.hsaId}",
                                    ItemType.CONSUMER
                                )
                            )
                        }
                        add(HippoTabulator(consumerHeading, viewConsumerLst))
                    }.apply {
                        width = 20.perc
                    }
                    // Service contracts
                    cell {
                        val viewContractLst: MutableList<ViewInformation> = mutableListOf()
                        state.vDomainsAndContracts.map {
                            if (it::class.simpleName == "ServiceDomain") {
                                val desc = "<b>${it.description}</b>"
                                viewContractLst.add(ViewInformation(it, desc, ItemType.DOMAIN))
                            } else {
                                val desc = it.description
                                viewContractLst.add(ViewInformation(it, desc, ItemType.CONTRACT))
                            }
                        }
                        add(HippoTabulator(contractHeading, viewContractLst))
                    }.apply {
                        width = 20.perc
                    }
                    // Plattforms
                    cell {
                        val viewPlattformList: MutableList<ViewInformation> = mutableListOf()
                        state.vPlattformChains.map {
                            viewPlattformList.add(
                                ViewInformation(
                                    it,
                                    it.name,
                                    ItemType.PLATTFORM_CHAIN
                                )
                            )
                        }
                        add(HippoTabulator(plattformHeading, viewPlattformList))
                    }.apply {
                        width = 15.perc
                    }
                    // Logical addresses
                    cell {
                        val viewLogicalAddressList: MutableList<ViewInformation> = mutableListOf()
                        state.vLogicalAddresses.map {
                            viewLogicalAddressList.add(
                                ViewInformation(
                                    it,
                                    "<i>${it.description}</i><br>${it.name}",
                                    ItemType.LOGICAL_ADDRESS
                                )
                            )
                        }
                        add(HippoTabulator(logicalAddressHeading, viewLogicalAddressList))
                    }.apply {
                        width = 20.perc
                    }
                    // Service producers
                    cell {
                        val viewProducerLst: MutableList<ViewInformation> = mutableListOf()
                        state.vServiceProducers.map {
                            viewProducerLst.add(
                                ViewInformation(
                                    it,
                                    "<i>${it.description}</i><br>${it.hsaId}",
                                    ItemType.PRODUCER
                                )
                            )
                        }
                        add(HippoTabulator(producerHeading, viewProducerLst))
                    }.apply {
                        width = 20.perc
                    }
                }.apply {
                    color = Color(Col.BLACK)
                    width = 100.perc
                }
            }.apply {
                color = Color(0x009090)
                width = 100.perc
            }
        }
    }
}

class HippoTabulator(

    columnHeader: String,
    itemList: List<ViewInformation>
) : SimplePanel() {
    init {
        tabulator(
            itemList,
            options = TabulatorOptions(
                columns = listOf(
                    ColumnDefinition(
                        columnHeader, "showData",

                        formatterComponentFunction = { _, _, item ->
                            //console.log("In ColumnDef: ${item.name}")
                            Div(rich = true) {
                                if (store.getState().isItemFiltered(item.type, item.baseItem.id)) {
                                    background = Background(Col.LIGHTSTEELBLUE)
                                }
                                +item.showData
                            }
                            //Div() { +item.hsaId }
                        },
                        headerFilter = Editor.INPUT
                    )
                ),
                layout = Layout.FITCOLUMNS,
                layoutColumnsOnNewData = false,
                pagination = PaginationMode.LOCAL,
                height = "80vh",
                paginationSize = 100,
                selectable = true,
                rowSelected = { row ->
                    console.log(row)
                    val viewItem = row.getData() as ViewInformation
                    val viewType = viewItem.type
                    val baseItem = viewItem.baseItem
                    store.dispatch { dispatch, getState ->
                        store.dispatch(HippoAction.ItemSelected(viewType, baseItem))
                        println("Time to download the integrations since an item has been selected")
                        loadIntegrations(getState())
                    }
                    println("Item clicked: $viewType")
                    console.log(baseItem)
                }
            )
        )
        {
            //height = 430.px
            width = 90.perc
        }
    }
}

// Let the URL mirror the current state
fun setUrlFilter(bookmark: String) {
    if (bookmark.length > 1) {
        val hostname = window.location.hostname;
        val protocol = window.location.protocol;
        val port = window.location.port;

        val portSpec = if (port.length > 0) ":$port" else ""

        val newUrl = protocol + "//" + hostname + portSpec + "?filter=" + bookmark

        console.log("New URL: " + newUrl)
        window.history.pushState(newUrl, "hippo-utforska integrationer", newUrl)
    }
}

// Parse the filter parameter and set the state accordingly
