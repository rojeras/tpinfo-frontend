package se.skoview.view

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.form.Form
import pl.treksoft.kvision.form.formPanel
import pl.treksoft.kvision.form.text.Text
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.form.text.textInput
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.html.div
import pl.treksoft.kvision.html.span
import pl.treksoft.kvision.i18n.I18n
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.state.ObservableList
import pl.treksoft.kvision.state.observableListOf
import pl.treksoft.kvision.state.stateBinding
import pl.treksoft.kvision.table.*
import pl.treksoft.kvision.tabulator.*
import pl.treksoft.kvision.utils.em
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px
import se.skoview.data.*
import kotlin.math.min

data class SearchFilter(
    var consumerSearchFilter: String = ""
)

val searchFilters: ObservableList<SearchFilter> = observableListOf(SearchFilter(""))

class HippoTablePage(
    val consumerHeading: String,
    val contractHeading: String,
    val plattformHeading: String,
    val logicalAddressHeading: String,
    val producerHeading: String,
    val viewConsumerLst: MutableList<ViewInformation>,
    val viewContractLst: MutableList<ViewInformation>,
    val viewPlattformList: MutableList<ViewInformation>,
    val viewLogicalAddressList: MutableList<ViewInformation>,
    val viewProducerLst: MutableList<ViewInformation>
) : SimplePanel() {
    init {
        println("In HippoTablePage")
        table(
            listOf(
                consumerHeading,
                contractHeading,
                plattformHeading,
                logicalAddressHeading,
                producerHeading
            ),
            setOf(TableType.SMALL, TableType.BORDERLESS),
            classes = setOf("table-layout:fixed")
        ).stateBinding(store) { state ->
            println("In HippoTablePage - after stateBinding()")
            row(classes = setOf("table-layout:fixed")) {
                // Service consumers
                cell(classes = setOf("table-layout:fixed")) {
                    textInput(type = TextInputType.SEARCH) {
                        placeholder = "Sök tjänstekonsument..."
                        onEvent {
                            input = {
                                //println(self.value ?: "")
                                store.dispatch(HippoAction.FilterConsumers(self.value ?: ""))
                            }
                        }
                    }
                    add(HippoTable(viewConsumerLst.filter { it.showData.contains(state.consumerFilter) }))
                }.apply {
                    width = 20.perc
                }
                // Service contracts
                cell {
                    span("Search box")
                    add(HippoTable(viewContractLst))
                }.apply {
                    width = 20.perc
                }
                // Plattforms
                cell {
                    span("Search box")
                    add(HippoTable(viewPlattformList))
                }.apply {
                    width = 15.perc
                }
                // Logical addresses
                cell {
                    span("Search box")
                    add(HippoTable(viewLogicalAddressList))
                }.apply {
                    width = 20.perc
                }
                // Service producers
                cell {
                    span("Search box")
                    add(HippoTable(viewProducerLst))
                }.apply {
                    width = 20.perc
                }
            }.apply {
                color = Color(Col.BLACK)
                width = 100.perc
            }

        }
    }
}

class HippoTable(
    itemList: List<ViewInformation>
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
                                    store.dispatch { dispatch, getState ->
                                        store.dispatch(HippoAction.ItemSelected(viewType, baseItem))
                                        println("Time to download the integrations since an item has been selected/deselected")
                                        console.log(getState())
                                        loadIntegrations(getState())
                                    }
                                    println("Item clicked: $viewType")
                                    console.log(baseItem)
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