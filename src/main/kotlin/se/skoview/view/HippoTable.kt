package se.skoview.view

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.form.InputSize
import pl.treksoft.kvision.form.select.selectInput
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.form.text.textInput
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.hPanel
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.state.stateBinding
import pl.treksoft.kvision.table.*
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.utils.vw
import se.skoview.data.*
import kotlin.math.min

data class SearchFilter(
    var consumerSearchFilter: String = ""
)

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
            div(classes = setOf(""""class="cssload-loader"""")).width = 100.perc
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
                            store.dispatch { dispatch, getState ->
                                dispatch(HippoAction.DateSelected(self.value ?: ""))
                                loadIntegrations(getState())
                            }
                        }
                    }
            }//.width = 100.perc
        }

        hPanel {
            // The whole item table

            // -------------------------------------------------------------------------------------------------------
            // Consumers
            vPanel {
                width = 20.vw
                div {
                    width = 100.perc
                    minWidth = 100.perc
                    searchField(ItemType.CONSUMER)
                }
                div {}.stateBinding(store) { state ->
                    h5("Tjänstekonsumenter (${state.vServiceConsumers.size}/${state.maxCounters.consumers})")
                    div {
                        state.vServiceConsumers.subList(0, min(state.vServiceConsumers.size, 100))
                            .map {
                                div(
                                    rich = true
                                ) {
                                    wordBreak = WordBreak.BREAKALL
                                    border = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                                    val item = it
                                    if (store.getState().isItemFiltered(ItemType.CONSUMER, item.id)) background =
                                        Background(Col.LIGHTSTEELBLUE)

                                    val content = "<i>${it.description}</i><br>${item.hsaId}"
                                    +"""<span style=word-break:break-all;>${content}</span>"""

                                    onEvent {
                                        click = {
                                            store.dispatch { dispatch, getState ->
                                                dispatch(HippoAction.ItemSelected(ItemType.CONSUMER, item))
                                                createViewData(getState())
                                            }
                                        }
                                    }
                                }
                            }
                    }
                }
            }

            // -------------------------------------------------------------------------------------------------------
            // Domains and contracts
            vPanel {
                div {
                    width = 20.vw
                    searchField(ItemType.CONTRACT)
                }
                div {}.stateBinding(store) { state ->
                    h5("Tjänstekontrakt (${state.vServiceContracts.size}/${state.maxCounters.contracts})")
                    div {
                        border = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                        state.vDomainsAndContracts.subList(0, min(state.vDomainsAndContracts.size, 100))
                            .map {
                                div(
                                    rich = true
                                ) {
                                    wordBreak = WordBreak.BREAKALL
                                    //border = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                                    val item = it
                                    // Service Contract
                                    if (item::class.simpleName == "ServiceContract") {
                                        //border = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                                        if (store.getState().isItemFiltered(ItemType.CONTRACT, item.id)) background =
                                            Background(Col.LIGHTSTEELBLUE)

                                        val content = it.description
                                        +"""<span style=word-break:break-all; word-wrap: break-word; "onMouseOver=this.style.cursor='hand'";>${content}</span>"""

                                        onEvent {
                                            click = {
                                                store.dispatch { dispatch, getState ->
                                                    dispatch(HippoAction.ItemSelected(ItemType.CONTRACT, item))
                                                    createViewData(getState())
                                                }
                                            }
                                        }
                                    } else {
                                        // Service Domain
                                        border = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                                        if (store.getState().isItemFiltered(ItemType.DOMAIN, item.id)) background =
                                            Background(Col.LIGHTSTEELBLUE)

                                        val content = "<b>${it.description}</b>"
                                        +"""<span style=word-break:break-all; word-wrap: break-word; "onMouseOver=this.style.cursor='hand'";>${content}</span>"""

                                        onEvent {
                                            click = {
                                                store.dispatch { dispatch, getState ->
                                                    dispatch(HippoAction.ItemSelected(ItemType.DOMAIN, item))
                                                    createViewData(getState())
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                    }
                }
            }
            // -------------------------------------------------------------------------------------------------------
            // Plattform chains
            vPanel {
                div {
                    width = 15.vw
                    searchField(ItemType.PLATTFORM_CHAIN)
                }
                div {}.stateBinding(store) { state ->
                    h5("Tjänsteplattformar (${state.vPlattformChains.size}/${state.maxCounters.plattformChains})")
                    div {
                        state.vPlattformChains.subList(0, min(state.vPlattformChains.size, 100))
                            .map {
                                div(
                                    rich = true
                                ) {
                                    wordBreak = WordBreak.BREAKALL
                                    border = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                                    val item = it
                                    if (store.getState().isItemFiltered(ItemType.PLATTFORM_CHAIN, item.id)) background =
                                        Background(Col.LIGHTSTEELBLUE)

                                    val content = item.name
                                    +"""<span style=word-break:break-all; word-wrap: break-word; "onMouseOver=this.style.cursor='hand'";>${content}</span>"""

                                    onEvent {
                                        click = {
                                            store.dispatch { dispatch, getState ->
                                                dispatch(HippoAction.ItemSelected(ItemType.PLATTFORM_CHAIN, item))
                                                createViewData(getState())
                                            }
                                        }
                                    }
                                }
                            }
                    }
                } //.apply { width = 100.perc }
            }


            // -------------------------------------------------------------------------------------------------------
            // Logical Addresses
            vPanel {
                div {
                    width = 20.vw
                    searchField(ItemType.LOGICAL_ADDRESS)
                }
                div {}.stateBinding(store) { state ->
                    h5("Logiska adresser (${state.vLogicalAddresses.size}/${state.maxCounters.logicalAddress})")
                    div {
                        state.vLogicalAddresses.subList(0, min(state.vLogicalAddresses.size, 100))
                            .map {
                                div(
                                    rich = true
                                ) {
                                    wordBreak = WordBreak.BREAKALL
                                    border = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                                    val item = it
                                    if (store.getState().isItemFiltered(ItemType.LOGICAL_ADDRESS, item.id)) background =
                                        Background(Col.LIGHTSTEELBLUE)

                                    val content = "<i>${it.description}</i><br>${item.name}"
                                    +"""<span style=word-break:break-all; word-wrap: break-word; "onMouseOver=this.style.cursor='hand'";>${content}</span>"""

                                    onEvent {
                                        click = {
                                            store.dispatch { dispatch, getState ->
                                                dispatch(HippoAction.ItemSelected(ItemType.LOGICAL_ADDRESS, item))
                                                createViewData(getState())
                                            }
                                        }
                                    }
                                }
                            }
                    }
                } //.apply { width = 100.perc }
            }

            // -------------------------------------------------------------------------------------------------------
            // Producers
            vPanel {
                div {
                    minWidth = 100.perc
                    searchField(ItemType.PRODUCER)
                }
                div {}.stateBinding(store) { state ->
                    h5("Tjänsteproducenter (${state.vServiceProducers.size}/${state.maxCounters.producers})")
                    div {
                        state.vServiceProducers.subList(0, min(state.vServiceProducers.size, 100))
                            .map {
                                div(
                                    rich = true
                                ) {
                                    wordBreak = WordBreak.BREAKALL
                                    border = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                                    val item = it
                                    if (store.getState().isItemFiltered(ItemType.PRODUCER, item.id)) background =
                                        Background(Col.LIGHTSTEELBLUE)

                                    val content = "<i>${it.description}</i><br>${item.hsaId}"
                                    +"""<span style=word-break:break-all; word-wrap: break-word; "onMouseOver=this.style.cursor='hand'";>${content}</span>"""

                                    onEvent {
                                        click = {
                                            store.dispatch { dispatch, getState ->
                                                dispatch(HippoAction.ItemSelected(ItemType.PRODUCER, item))
                                                createViewData(getState())
                                            }
                                        }
                                    }
                                }
                            }
                    }
                } //.apply { width = 100.perc }
            }
        }
    }
}

private fun Container.searchField(type: ItemType) {
    textInput(type = TextInputType.SEARCH) {
        if (type == ItemType.CONSUMER) placeholder = "Sök tjänstekonsument..."
        else if (type == ItemType.CONTRACT) placeholder = "Sök tjänstekontrakt/tjänstedomän..."
        else if (type == ItemType.LOGICAL_ADDRESS) placeholder = "Sök logisk adress..."
        else if (type == ItemType.PRODUCER) placeholder = "Sök tjänsteproducent..."
        else if (type == ItemType.PLATTFORM_CHAIN) placeholder = "Sök tjänsteplattform(ar)..."
        else placeholder == "Internal error in searchField()"

        onEvent {
            var timeout = 0
            input = {
                kotlin.browser.window.clearTimeout(timeout)
                val value = self.value ?: ""
                timeout = kotlin.browser.window.setTimeout({
                    store.dispatch { dispatch, getState ->
                        println("::::::::::::::> Current field: ${self.value}")
                        if (value.length > 0) self.background = Background(Col.LIGHTCYAN)
                        else self.background = Background(Col.WHITE)
                        dispatch(HippoAction.FilterItems(type, value))
                        createViewData(getState())
                    }
                }, 500)
            }
        }
    }
}

