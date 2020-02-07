package se.skoview.view

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.form.InputSize
import pl.treksoft.kvision.form.select.selectInput
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.form.text.textInput
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.state.ObservableList
import pl.treksoft.kvision.state.observableListOf
import pl.treksoft.kvision.state.stateBinding
import pl.treksoft.kvision.table.*
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px
import se.skoview.data.*
import kotlin.math.min

data class SearchFilter(
    var consumerSearchFilter: String = ""
)

val searchFilters: ObservableList<SearchFilter> = observableListOf(SearchFilter(""))

data class ViewTableInformation(
    val baseItem: BaseItem,
    val showData: String,
    val type: ItemType
)

object hippoTablePage : SimplePanel() {

    init {

        println(">>> In hippoPage")

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
        }

        println(">>> In HippoTablePage")
        table(
            listOf(),
            setOf(TableType.SMALL, TableType.BORDERLESS),
            classes = setOf("table-layout:fixed")
            //).stateBinding(store) { state ->
        ).stateBinding(store) { state ->
            println(">>> In HippoTablePage - recreate the view")

            row(classes = setOf("table-layout:fixed")) {
                cell { h5("Tjänstekonsumenter (${state.vServiceConsumers.size}/${state.maxCounters.consumers})") }.apply {
                    width = 20.perc
                }
                cell { h5("Tjänstekontrakt (${state.vServiceContracts.size}/${state.maxCounters.contracts})") }.apply {
                    width = 20.perc
                }
                cell { h5("Tjänsteplattformar (${state.vPlattformChains.size}/${state.maxCounters.plattformChains})") }.apply {
                    width = 15.perc
                }
                cell { h5("Logiska adresser (${state.vLogicalAddresses.size}/${state.maxCounters.logicalAddress})") }.apply {
                    width = 20.perc
                }
                cell { h5("Tjänsteproducenter (${state.vServiceProducers.size}/${state.maxCounters.producers})") }.apply {
                    width = 20.perc
                }
            }
            row(classes = setOf("table-layout:fixed")) {
                cell { searchField() }
                cell { span("Sökfält") }
                cell { span("Sökfält") }
                cell { span("Sökfält") }
                cell { span("Sökfält") }
            }
            row(classes = setOf("table-layout:fixed")) {
                cell(classes = setOf("table-layout:fixed")) {
                    div {
                        state.vServiceConsumers.subList(0, min(state.vServiceConsumers.size, 100)).map {
                            div(
                                rich = true
                            ) {
                                border = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                                val item = it
                                if (store.getState().isItemFiltered(ItemType.CONSUMER, item.id)) background =
                                    Background(Col.LIGHTSTEELBLUE)

                                val content = "<i>${it.description}</i><br>${item.hsaId}"
                                +"""<span style=word-break:break-all; word-wrap: break-word; "onMouseOver=this.style.cursor='hand'";>${content}</span>"""
                                onEvent {
                                    click = {
                                        store.dispatch(HippoAction.ItemSelected(ItemType.CONSUMER, item))
                                    }
                                }
                            }
                        }.apply { width = 100.perc }
                    }
                }.apply { width = 20.perc }

                // Service contracts
                cell {
                    val viewContractLst: MutableList<ViewTableInformation> = mutableListOf()
                    state.vDomainsAndContracts.map {
                        if (it::class.simpleName == "ServiceDomain") {
                            val desc = "<b>${it.description}</b>"
                            viewContractLst.add(ViewTableInformation(it, desc, ItemType.DOMAIN))
                        } else {
                            val desc = it.description
                            viewContractLst.add(ViewTableInformation(it, desc, ItemType.CONTRACT))
                        }
                    }
                    add(HippoTable(viewContractLst))
                }.apply {
                    width = 20.perc
                }
                // Plattforms
                cell {
                    val viewPlattformList: MutableList<ViewTableInformation> = mutableListOf()
                    state.vPlattformChains.map {
                        viewPlattformList.add(
                            ViewTableInformation(
                                it,
                                it.name,
                                ItemType.PLATTFORM_CHAIN
                            )
                        )
                    }
                    add(HippoTable(viewPlattformList))
                }.apply {
                    width = 15.perc
                }
                // Logical addresses
                cell {
                    val viewLogicalAddressList: MutableList<ViewTableInformation> = mutableListOf()
                    state.vLogicalAddresses.map {
                        viewLogicalAddressList.add(
                            ViewTableInformation(
                                it,
                                "<i>${it.description}</i><br>${it.name}",
                                ItemType.LOGICAL_ADDRESS
                            )
                        )
                    }
                    add(HippoTable(viewLogicalAddressList))
                }.apply {
                    width = 20.perc
                }
                // Service producers
                cell {
                    val viewProducerLst: MutableList<ViewTableInformation> = mutableListOf()
                    state.vServiceProducers.map {
                        viewProducerLst.add(
                            ViewTableInformation(
                                it,
                                "<i>${it.description}</i><br>${it.hsaId}",
                                ItemType.PRODUCER
                            )
                        )
                    }
                    add(HippoTable(viewProducerLst))
                }.apply {
                    width = 20.perc
                }
                /*
            }.apply
            {
                color = Color(Col.BLACK)
                width = 100.perc
            }
*/
            }
        }
    }
}

class HippoTable(
    itemList: List<ViewTableInformation>
) : SimplePanel() {
    init {
        div {
            border = Border(1.px, BorderStyle.SOLID, Col.BLACK)
            table(
                listOf(),
                setOf(TableType.SMALL, TableType.BORDERLESS),
                responsiveType = ResponsiveType.RESPONSIVE
                //setOf(TableType.SMALL, TableType.BORDERED)
            ) {
                itemList
                    .subList(0, min(itemList.size, 100))
                    .map {
                        div(
                            rich = true
                        ) {
                            val item = it

                            if (it.type == ItemType.CONTRACT) {
                                //border = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                            } else {
                                border = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                            }
                            if (store.getState().isItemFiltered(item.type, item.baseItem.id)) {
                                background = Background(Col.LIGHTSTEELBLUE)
                            }

                            +"""<span style=word-break:break-all; word-wrap: break-word; "onMouseOver=this.style.cursor='hand'";>${it.showData}</span>"""

                            onEvent {
                                click = {
                                    println("Clicked: ${item.showData}")
                                    val viewType = item.type
                                    val baseItem = item.baseItem
                                    //store.dispatch { dispatch, getState ->
                                    store.dispatch(HippoAction.ItemSelected(viewType, baseItem))
                                    //println("Time to download the integrations since an item has been selected/deselected")
                                    //console.log(getState())
                                    //loadIntegrations(getState())
                                    //}
                                }
                            }

                        }
                    }
            }.apply {
                width = 100.perc
            }
        }
    }
}

private fun Container.searchField() {
    textInput(type = TextInputType.SEARCH) {
        placeholder = "Sök tjänstekonsument..."
        onEvent {
            input = {
                println("Before dispatch: ${self.value ?: ""}")
                //store.dispatch { dispatch, getState ->
                    store.dispatch(HippoAction.FilterConsumers(self.value ?: ""))
                //}
                println("After dispatch")
            }
        }
    }.apply { value = store.getState().consumerFilter }
}