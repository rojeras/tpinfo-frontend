package se.skoview.view

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.form.InputSize
import pl.treksoft.kvision.form.select.selectInput
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.form.text.textInput
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.modal.Modal
import pl.treksoft.kvision.modal.ModalSize
import pl.treksoft.kvision.panel.*
import pl.treksoft.kvision.state.stateBinding
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.utils.vw
import se.skoview.app.store
import se.skoview.data.*
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.min

object HippoTablePage : SimplePanel() {

    init {
        // font-family: Georgia,Times New Roman,Times,serif;
        fontFamily = "Times New Roman"
        // Page header
        vPanel {
            div {
                h2("Hippo - integrationer via tjänsteplattform/ar för nationell e-hälsa")
                div("Integrationer för tjänsteplattformar vars tjänstadresseringskatalog (TAK) är tillgänglig i Ineras TAK-api visas.")
            }.apply {
                //width = 100.perc
                background = Background(0x113d3d)
                align = Align.CENTER
                color = Color(Col.WHITE)
                marginTop = 5.px
            }
        }
        // Date selector

        //hPanel { clear = Clear.BOTH
        flexPanel(
            FlexDir.ROW, FlexWrap.WRAP, FlexJustify.SPACEBETWEEN, FlexAlignItems.CENTER,
            spacing = 5
        ) {
            clear = Clear.BOTH
            margin = 0.px
            background = Background(0xf6efe9)
            div {
                align = Align.LEFT
                //background = Background(Col.BLUEVIOLET)
            }.stateBinding(store) { state ->
                // Lets scroll to top here
                document.body!!.scrollTop = 0.toDouble()
                document.documentElement!!.scrollTop = 0.toDouble() // All other browsers
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

            // Statistics button
            div {
                align = Align.CENTER
                //background = Background(Col.LIGHTSTEELBLUE)
            }.stateBinding(store) { state ->
                val sllRtpProdId = 3
                val chainId = if (state.vPlattformChains.size == 1) state.vPlattformChains[0].id else -1
                if (chainId > 0) {
                    val chain = PlattformChain.map[chainId]
                    if (chain!!.first == sllRtpProdId || chain.last == sllRtpProdId) {
                        button("SLL statistiktjänst", style = ButtonStyle.INFO).onClick {
                            size = ButtonSize.SMALL
                            window.open("https://statistik.tjansteplattform.se/", "_blank")
                        }.apply {
                            addBsBgColor(BsBgColor.INFO)
                            addBsColor(BsColor.WHITE)
                        }
                    }
                }
            }

            // About button
            div {
                //background = Background(Col.LIGHTSKYBLUE)
                align = Align.RIGHT
                val modal = Modal("Om hippo")
                modal.iframe(src = "about.html", iframeHeight = 400, iframeWidth = 700)
                modal.size = ModalSize.LARGE
                //modal.add(H(require("img/dog.jpg")))
                modal.addButton(Button("Stäng").onClick {
                    modal.hide()
                })
                button("Om hippo", style = ButtonStyle.INFO).onClick {
                    size = ButtonSize.SMALL
                    modal.show()
                }.apply {
                    addBsBgColor(BsBgColor.LIGHT)
                    addBsColor(BsColor.BLACK50)
                }
            }
        }

        // The whole item table
        hPanel {
            background = Background(0xffffff)
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

                    val textFilter = state.contractFilter
                    val filteredList = state.vDomainsAndContracts.filter { it.searchField.isEmpty() || it.searchField.contains(textFilter, true) }

                    h5("<b>Tjänstekontrakt (${filteredList.size}/${state.maxCounters.contracts})</b>").apply {
                        color = Color(0x227777)
                        rich = true
                    }
                    div {
                        borderLeft = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                        borderRight = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                        borderBottom = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                        if (filteredList.size < state.vDomainsAndContracts.size) background = Background(Col.LIGHTGRAY)
                        filteredList.map { item ->
                                div(
                                    classes = setOf("pointer"),
                                    rich = true
                                ) {

                                    wordBreak = WordBreak.BREAKALL
                                    // Service Contract
                                    if (item::class.simpleName == "ServiceContract") {
                                        +item.description
                                        //if (store.getState().isItemFiltered(ItemType.CONTRACT, item.id)) {
                                        if (
                                            state.isItemFiltered(ItemType.CONTRACT, item.id) &&
                                            state.vServiceContracts.size == 1
                                        ) {
                                            insertResetButton(item, ItemType.CONTRACT)
                                        } else itemSelect(item, ItemType.CONTRACT)
                                    } else {
                                        // Service Domain
                                        +"<b>${item.description}</b>"
                                        borderTop = Border(1.px, BorderStyle.SOLID, Col.GRAY)

                                        if (
                                            state.isItemFiltered(ItemType.DOMAIN, item.id) &&
                                            state.vServiceDomains.size == 1
                                        ) {
                                            //background = Background(Col.LIGHTSTEELBLUE)
                                            insertResetButton(item, ItemType.DOMAIN)
                                        } else itemSelect(item, ItemType.DOMAIN)
                                    }
                                }
                            }
                    }
                }
            }
            // -------------------------------------------------------------------------------------------------------
            add(HippoItemsView(ItemType.PLATTFORM_CHAIN, "Tjänsteplattformar", 18))
            add(
                HippoItemsView(
                    ItemType.PRODUCER,
                    "Tjänsteproducenter"
                )
            )
            add(
                HippoItemsView(
                    ItemType.LOGICAL_ADDRESS,
                    "Logiska adresser"
                )
            )

        }
    }
}

class HippoItemsView(type: ItemType, heading: String, bredd: Int = 20) : VPanel() {
    init {
        //background = Background(Col.LIGHTCYAN)
        width = bredd.vw
        margin = 3.px
        div {
            searchField(type)
        }
        div {}.stateBinding(store) { state ->
            div {
                // todo: Kolla om dessa kan göras till val eller defieras inom if-satsen nedan
                var vList = listOf<BaseItem>()
                var maxCounter = -1
                var maxNoItems = -1
                var textFilter = ""

                //background = Background(Col.LIGHTSTEELBLUE)
                when (type) {
                    ItemType.CONSUMER -> {
                        vList = state.vServiceConsumers
                        maxCounter = state.maxCounters.consumers
                        maxNoItems = state.vServiceConsumersMax
                        textFilter = state.consumerFilter
                    }
                    ItemType.PRODUCER -> {
                        vList = state.vServiceProducers
                        maxCounter = state.maxCounters.producers
                        maxNoItems = state.vServiceProducersMax
                        textFilter = state.producerFilter
                    }
                    ItemType.PLATTFORM_CHAIN -> {
                        vList = state.vPlattformChains
                        maxCounter = state.maxCounters.plattformChains
                        maxNoItems = 100
                        textFilter = state.plattformChainFilter
                    }
                    ItemType.LOGICAL_ADDRESS -> {
                        vList = state.vLogicalAddresses
                        maxCounter = state.maxCounters.logicalAddress
                        maxNoItems = state.vLogicalAddressesMax
                        textFilter = state.logicalAddressFilter
                    }
                    else -> println("Error in HippoItemsView class, type = $type")
                }

                val filteredList = vList.filter { it.searchField.isEmpty() || it.searchField.contains(textFilter, true) }

                h5("<b>$heading (${filteredList.size}/${maxCounter})</b>")
                    .apply {
                        color = Color(0x227777)
                        rich = true
                    }
                div {
                    borderLeft = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                    borderRight = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                    borderBottom = Border(1.px, BorderStyle.SOLID, Col.GRAY)
                    if (filteredList.size < vList.size) background = Background(Col.LIGHTGRAY)

                    //vList.filter { it.searchField.isEmpty() || it.searchField.contains(textFilter, true) }

                    filteredList
                        .subList(0, min(filteredList.size, maxNoItems))
                        .map { item ->
                                div(
                                    classes = setOf("pointer"),
                                    rich = true
                                ) {
                                    val itemText = if (type == ItemType.PLATTFORM_CHAIN) item.name
                                    else "<i>${item.description}</i><br>${item.name}"

                                    +itemText

                                    wordBreak = WordBreak.BREAKALL
                                    borderTop = Border(1.px, BorderStyle.SOLID, Col.GRAY)

                                    if (
                                        state.isItemFiltered(type, item.id) &&
                                        vList.size == 1
                                    ) {
                                        //background = Background(Col.LIGHTSTEELBLUE)
                                        insertResetButton(item, type)
                                    } else itemSelect(item, type)
                                }
                        }
                }
                showMoreItemsButton(type, filteredList.size, maxNoItems)
            }
        }
    }
}

private fun Container.searchField(type: ItemType, currentValue: String = "") {
    textInput(type = TextInputType.SEARCH) {
        when (type) {
            ItemType.CONSUMER -> placeholder = "Sök tjänstekonsument..."
            ItemType.CONTRACT -> placeholder = "Sök tjänstekontrakt/domän..."
            ItemType.LOGICAL_ADDRESS -> placeholder = "Sök logisk adress..."
            ItemType.PRODUCER -> placeholder = "Sök tjänsteproducent..."
            ItemType.PLATTFORM_CHAIN -> placeholder = "Sök tjänsteplattform(ar)..."
            else -> placeholder == "Internal error in searchField()"
        }
        //value = currentValue
        onEvent {
            var timeout = 0
            input = {
                window.clearTimeout(timeout)
                val value = self.value ?: ""
                timeout = window.setTimeout({
                    store.dispatch { dispatch, getState ->
                        println("::::::::::::::> Current field: ${self.value}")
                        if (value.isNotEmpty()) self.background = Background(Col.LIGHTGRAY)
                        else self.background = Background(Col.WHITE)
                        dispatch(HippoAction.FilterItems(type, value))
                        //createViewData(getState())
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
                store.dispatch(HippoAction.ItemSelected(type, item))
                //createViewData(getState())
                loadIntegrations(getState())
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
    }
    div {
        button(buttonText, style = ButtonStyle.PRIMARY) {
            width = 100.perc
            background = Background(Col.GRAY)
            onClick {
                store.dispatch { dispatch, getState ->
                    dispatch(HippoAction.ItemSelected(type, item))
                    //createViewData(getState())
                    loadIntegrations(getState())
                }
            }
        }
    }
}

private fun Container.showMoreItemsButton(type: ItemType, size: Int, maxItemsToShow: Int) {
    if (size > maxItemsToShow) {
        div {
            //color = Color(Col.RED)
            //+"Ytterligare ${size - maxItemsToShow} rader tillgängliga via sökning eller filtrering"
            val linesLeft = size - maxItemsToShow
            val moreLinesToShow = min(linesLeft, 500)
            val actualLinesToShow = moreLinesToShow + maxItemsToShow
            button("Visa ytterligare ${moreLinesToShow} rader", style = ButtonStyle.PRIMARY) {
                width = 100.perc
                background = Background(Col.GRAY)
                onClick {
                    store.dispatch { dispatch, getState ->
                        dispatch(HippoAction.SetVMax(type, actualLinesToShow))
                        createViewData(getState())

                    }
                }
            }
        }
    }
}