package se.skoview.view

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.form.InputSize
import pl.treksoft.kvision.form.select.selectInput
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.form.text.textInput
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.panel.hPanel
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.state.stateBinding
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.utils.vw
import se.skoview.app.store
import se.skoview.data.*
import kotlin.math.min

object HippoTablePage : SimplePanel() {

    init {
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
            clear = Clear.BOTH
            margin = 0.px
            background = Background(0xf6efe9)
        }.stateBinding(store) { state ->
            //div(classes = setOf(""""class="cssload-loader"""")).width = 100.perc
            div {
                height = 40.px
                val dateOptionList = state.updateDates.map { Pair(it, it) }
                selectInput(
                    options = dateOptionList,
                    value = state.dateEffective
                ) {
                    selectWidth = CssSize(150, UNIT.px)
                    size = InputSize.SMALL
                }.onEvent {
                    change = {
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
            background = Background(0xf2ffff)
            // -------------------------------------------------------------------------------------------------------
            // Consumers

            add(HippoItemsView(ItemType.CONSUMER, "Tjänstekonsumenter"))

            // -------------------------------------------------------------------------------------------------------
            // Domains and contracts
            vPanel {
                //background = Background(Col.LIGHTCYAN)
                width = 20.vw
                margin = 3.px
                div {
                    searchField(ItemType.CONTRACT)
                }
                div {}.stateBinding(store) { state ->
                    h5("Tjänstekontrakt (${state.vServiceContracts.size}/${state.maxCounters.contracts})").apply {color = Color(0x227777)}
                    val maxItemsToShow = 1000
                    div {
                        border = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                        state.vDomainsAndContracts.subList(0, min(state.vDomainsAndContracts.size, maxItemsToShow))
                            .map { item ->
                                div(
                                    classes = setOf("pointer"),
                                    rich = true
                                ) {

                                    wordBreak = WordBreak.BREAKALL
                                    // Service Contract
                                    if (item::class.simpleName == "ServiceContract") {
                                        +item.description
                                        if (store.getState().isItemFiltered( ItemType.CONTRACT, item.id ) ) {
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
                    showMoreItemText(state.vServiceContracts.size, maxItemsToShow)
                }
            }
            // -------------------------------------------------------------------------------------------------------
            add(HippoItemsView(ItemType.PLATTFORM_CHAIN, "Tjänsteplattformar", 18))
            add(HippoItemsView(ItemType.PRODUCER, "Tjänsteproducenter"))
            add(HippoItemsView(ItemType.LOGICAL_ADDRESS, "Logisk adresser"))
        }
    }
}

class HippoItemsView(type: ItemType, heading: String, bredd: Int = 20): VPanel() {
    init {
        //background = Background(Col.LIGHTCYAN)
        width = bredd.vw
        margin = 3.px
        div {
            searchField(type)
        }
        div {}.stateBinding(store) { state ->
            // todo: Kolla om dessa kan göras till val eller defieras inom if-satsen nedan
            var vList = listOf<BaseItem>()
            var maxCounter = -1

            //background = Background(Col.LIGHTSTEELBLUE)
            when (type) {
                ItemType.CONSUMER -> {
                    vList = state.vServiceConsumers
                    maxCounter = state.maxCounters.consumers
                }
                ItemType.PRODUCER -> {
                    vList = state.vServiceProducers
                    maxCounter = state.maxCounters.producers
                }
                ItemType.PLATTFORM_CHAIN -> {
                    vList = state.vPlattformChains
                    maxCounter = state.maxCounters.plattformChains
                }
                ItemType.LOGICAL_ADDRESS -> {
                    vList = state.vLogicalAddresses
                    maxCounter = state.maxCounters.logicalAddress
                }
                else -> println("Error in HippoItemsView class, type = $type")
            }

            h5("$heading (${vList.size}/${maxCounter})").apply {color = Color(0x227777)}
            div {
                vList.subList(0, min(vList.size, 100))
                    .map { item ->
                        div(
                            classes = setOf("pointer"),
                            rich = true
                        ) {
                            val itemText = if (type == ItemType.PLATTFORM_CHAIN) item.name
                            else "<i>${item.description}</i><br>${item.name}"

                            +itemText

                            wordBreak = WordBreak.BREAKALL
                            border = Border(1.px, BorderStyle.SOLID, Col.GRAY)

                            if (store.getState().isItemFiltered(type, item.id)) {
                                //background = Background(Col.LIGHTSTEELBLUE)
                                insertResetButton(item, type)
                            } else itemSelect(item, type)
                        }
                    }
            }
            showMoreItemText(vList.size)
        }
    }
}

private fun Container.searchField(type: ItemType) {
    textInput(type = TextInputType.SEARCH) {
        when (type) {
            ItemType.CONSUMER -> placeholder = "Sök tjänstekonsument..."
            ItemType.CONTRACT -> placeholder = "Sök tjänstekontrakt/domän..."
            ItemType.LOGICAL_ADDRESS -> placeholder = "Sök logisk adress..."
            ItemType.PRODUCER -> placeholder = "Sök tjänsteproducent..."
            ItemType.PLATTFORM_CHAIN -> placeholder = "Sök tjänsteplattform(ar)..."
            else -> placeholder == "Internal error in searchField()"
        }

        onEvent {
            var timeout = 0
            input = {
                kotlin.browser.window.clearTimeout(timeout)
                val value = self.value ?: ""
                timeout = kotlin.browser.window.setTimeout({
                    store.dispatch { dispatch, getState ->
                        println("::::::::::::::> Current field: ${self.value}")
                        if (value.isNotEmpty()) self.background = Background(Col.LIGHTGREEN)
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
    val buttonText = when (type) {
        ItemType.CONSUMER -> "Återställ tjänstekonsument"
        ItemType.CONTRACT -> "Återställ tjänstekontrakt"
        ItemType.DOMAIN -> "Återställ tjänstedomän"
        ItemType.LOGICAL_ADDRESS -> "Återställ logisk adress"
        ItemType.PRODUCER -> "Återställ tjänsteproducent"
        ItemType.PLATTFORM_CHAIN -> "Återställ tjänsteplattform(ar)"
        else ->  "Internal error in insertResetButton()"
    }
    div {
        button(buttonText, style = ButtonStyle.PRIMARY) {
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

private fun Container.showMoreItemText(size: Int, maxItemsToShow: Int = 100) {
    if (size > maxItemsToShow) {
        div {
            color = Color(Col.RED)
            +"Ytterligare ${size - 100} rader tillgängliga via sökning eller filtrering"
        }
    }
}