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
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.utils.vw
import se.skoview.app.store
import se.skoview.data.*
import kotlin.browser.document
import kotlin.math.min

object hippoTablePage : SimplePanel() {

    init {

        println(">>> In hippoPage")

        // Page header
        vPanel {
            div {
                h2("Hippo - integrationer via tjänsteplattform/ar för nationell e-hälsa")
                div("Integrationer för tjänsteplattformar vars tjänstadresseringskatalog (TAK) är tillgänglig i Ineras TAK-api visas.")
            }.apply {
                //width = 100.perc
                background = Background(Col.DARKCYAN)
                align = Align.CENTER
                color = Color(Col.WHITE)
                marginTop = 5.px
            }
        }
        // Date selector
        vPanel {
        }.apply {
            //width = 100.perc
            clear = Clear.BOTH
            margin = 0.px
            background = Background(0xf6efe9)
        }.stateBinding(store) { state ->
            div(classes = setOf(""""class="cssload-loader"""")).width = 100.perc
            div {
                height = 40.px
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
            }
        }

        // The whole item table
        hPanel {
            background = Background(Col.LIGHTGRAY)
            // -------------------------------------------------------------------------------------------------------
            // Consumers
            vPanel {
                background = Background(Col.LIGHTCYAN)
                width = 20.vw
                margin = 3.px
                div {
                    searchField(ItemType.CONSUMER)
                }
                div {}.stateBinding(store) { state ->
                    h5("Tjänstekonsumenter (${state.vServiceConsumers.size}/${state.maxCounters.consumers})")
                    div {

                        state.vServiceConsumers.subList(0, min(state.vServiceConsumers.size, 100))
                            .map { item ->
                                div(
                                    classes = setOf("pointer"),
                                    rich = true
                                ) {
                                    +"<i>${item.description}</i><br>${item.hsaId}"
                                    wordBreak = WordBreak.BREAKALL
                                    border = Border(1.px, BorderStyle.SOLID, Col.GRAY)

                                    if (store.getState().isItemFiltered(ItemType.CONSUMER, item.id)) {
                                        //background = Background(Col.LIGHTSTEELBLUE)
                                        insertResetButton(item, ItemType.CONSUMER)
                                    } else itemSelect(item, ItemType.CONSUMER)
                                }
                            }
                    }
                    showMoreItemText(state.vServiceConsumers.size)
                }
            }

            // -------------------------------------------------------------------------------------------------------
            // Domains and contracts
            vPanel {
                background = Background(Col.LIGHTCYAN)
                width = 20.vw
                margin = 3.px
                div {
                    searchField(ItemType.CONTRACT)
                }
                div {}.stateBinding(store) { state ->
                    h5("Tjänstekontrakt (${state.vServiceContracts.size}/${state.maxCounters.contracts})")
                    div {
                        border = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                        state.vDomainsAndContracts.subList(0, min(state.vDomainsAndContracts.size, 100))
                            .map { item ->
                                div(
                                    classes = setOf("pointer"),
                                    rich = true
                                ) {

                                    wordBreak = WordBreak.BREAKALL
                                    // Service Contract
                                    if (item::class.simpleName == "ServiceContract") {
                                        +item.description
                                        if (store.getState().isItemFiltered(
                                                ItemType.CONTRACT,
                                                item.id
                                            )
                                        ) {
                                            //background = Background(Col.LIGHTSTEELBLUE)
                                            insertResetButton(item, ItemType.CONTRACT)
                                        } else itemSelect(item, ItemType.CONTRACT)
                                    } else {
                                        // Service Domain
                                        +"<b>${item.description}</b>"
                                        border = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                                        if (store.getState().isItemFiltered(ItemType.DOMAIN, item.id)) {
                                            //background = Background(Col.LIGHTSTEELBLUE)
                                            insertResetButton(item, ItemType.DOMAIN)
                                        } else itemSelect(item, ItemType.DOMAIN)
                                    }
                                }
                            }
                    }
                    showMoreItemText(state.vServiceContracts.size)
                }
            }
            // -------------------------------------------------------------------------------------------------------
            // Plattform chains
            vPanel {
                background = Background(Col.LIGHTCYAN)
                width = 18.vw
                margin = 3.px
                div {
                    searchField(ItemType.PLATTFORM_CHAIN)
                }
                div {}.stateBinding(store) { state ->
                    h5("Tjänsteplattformar (${state.vPlattformChains.size}/${state.maxCounters.plattformChains})")
                    div {
                        state.vPlattformChains.subList(0, min(state.vPlattformChains.size, 100))
                            .map { item ->
                                div(
                                    classes = setOf("pointer"),
                                    rich = true
                                ) {
                                    +item.name
                                    wordBreak = WordBreak.BREAKALL
                                    border = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                                    align = Align.CENTER
                                    if (store.getState().isItemFiltered(
                                            ItemType.PLATTFORM_CHAIN,
                                            item.id
                                        )
                                    ) {
                                        //background = Background(Col.LIGHTSTEELBLUE)
                                        insertResetButton(item, ItemType.PLATTFORM_CHAIN)
                                    } else itemSelect(item, ItemType.PLATTFORM_CHAIN)
                                }
                            }
                    }
                    showMoreItemText(state.vPlattformChains.size)
                }
            }


            // -------------------------------------------------------------------------------------------------------
            // Producers
            vPanel {
                background = Background(Col.LIGHTCYAN)
                width = 20.vw
                margin = 3.px
                div {
                    searchField(ItemType.PRODUCER)
                }
                div {}.stateBinding(store) { state ->
                    h5("Tjänsteproducenter (${state.vServiceProducers.size}/${state.maxCounters.producers})")
                    div {
                        state.vServiceProducers.subList(0, min(state.vServiceProducers.size, 100))
                            .map { item ->
                                div(
                                    classes = setOf("pointer"),
                                    rich = true
                                ) {
                                    +"<i>${item.description}</i><br>${item.hsaId}"
                                    wordBreak = WordBreak.BREAKALL
                                    border = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                                    if (store.getState().isItemFiltered(ItemType.PRODUCER, item.id)) {
                                        //background = Background(Col.LIGHTSTEELBLUE)
                                        insertResetButton(item, ItemType.PRODUCER)
                                    } else itemSelect(item, ItemType.PRODUCER)
                                }
                            }
                    }
                    showMoreItemText(state.vServiceProducers.size)
                }
            }

            // -------------------------------------------------------------------------------------------------------
            // Logical Addresses
            vPanel {
                background = Background(Col.LIGHTCYAN)
                width = 20.vw
                margin = 3.px
                div {
                    searchField(ItemType.LOGICAL_ADDRESS)
                }
                div {}.stateBinding(store) { state ->
                    h5("Logiska adresser (${state.vLogicalAddresses.size}/${state.maxCounters.logicalAddress})")
                    div {
                        state.vLogicalAddresses.subList(0, min(state.vLogicalAddresses.size, 100))
                            .map { item ->
                                div(
                                    classes = setOf("pointer"),
                                    rich = true
                                ) {
                                    +"<i>${item.description}</i><br>${item.name}"
                                    wordBreak = WordBreak.BREAKALL
                                    border = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                                    if (store.getState().isItemFiltered(
                                            ItemType.LOGICAL_ADDRESS,
                                            item.id
                                        )
                                    ) {
                                        //background = Background(Col.LIGHTSTEELBLUE)
                                        insertResetButton(item, ItemType.LOGICAL_ADDRESS)
                                    } else itemSelect(item, ItemType.LOGICAL_ADDRESS)
                                }
                            }
                    }
                    showMoreItemText(state.vLogicalAddresses.size)
                }
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
                        if (value.length > 0) self.background = Background(Col.LIGHTGREEN)
                        else self.background = Background(Col.WHITE)
                        dispatch(HippoAction.FilterItems(type, value))
                        createViewData(getState())
                    }
                }, 500)
            }
        }
    }
}

private fun Div.itemSelect(item: BaseItem, type: ItemType) {
    onEvent {
        click = {
            store.dispatch { dispatch, getState ->
                dispatch(HippoAction.ItemSelected(type, item))
                createViewData(getState())
            }
        }
    }
}

private fun Div.insertResetButton(item: BaseItem, type: ItemType) {
    div {
        button("Återställ", style = ButtonStyle.PRIMARY) {
            width = 100.perc
            background = Background(Col.GRAY)
            onClick {
                store.dispatch { dispatch, getState ->
                    dispatch(HippoAction.ItemSelected(type, item))
                    createViewData(getState())
                }
            }
        }
    }
}

private fun Container.showMoreItemText(size: Int) {
    if (size > 100) {
        div {
            color = Color(Col.RED)
            +"Ytterligare ${size - 100} rader tillgängliga via sökning eller filtrering"
        }
    }
}