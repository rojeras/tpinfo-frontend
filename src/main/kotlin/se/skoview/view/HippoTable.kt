/**
 * Copyright (C) 2013-2020 Lars Erik Röjerås
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package se.skoview.view

import com.github.snabbdom._set
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.core.Color.Companion.hex
import pl.treksoft.kvision.form.InputSize
import pl.treksoft.kvision.form.select.selectInput
import pl.treksoft.kvision.form.text.TextInput
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
/*
        // ---------------------------------------------------------------------------------------------
        textInput(type = TextInputType.SEARCH) {
            onEvent {
                var timeout = 0
                input = {
                    window.clearTimeout(timeout)
                    val value = self.value ?: ""
                    timeout = window.setTimeout({
                       println("Search value: $value")
                    }, 500)
                }
            }
        }

        val aaa: String? = document.getElementById("sss")!!.innerHTML = "Hello"
        // ---------------------------------------------------------------------------------------------
 */


        // font-family: Georgia,Times New Roman,Times,serif;
        fontFamily = "Times New Roman"
        // Page header
        vPanel {
            div {
                h2("Hippo - integrationer via tjänsteplattform/ar för nationell e-hälsa")
                div("Integrationer för tjänsteplattformar vars tjänstadresseringskatalog (TAK) är tillgänglig i Ineras TAK-api visas.")
            }.apply {
                //width = 100.perc
                background = Background(Color.hex(0x113d3d))
                align = Align.CENTER
                color = Color.name(Col.WHITE)
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
            background = Background(Color.hex(0xf6efe9))
            div {
                align = Align.LEFT
                //background = Background(Col.BLUEVIOLET)
            }.stateBinding(store) { state ->
                // Lets scroll to top here
                //document.body!!.scrollTop = 0.toDouble()
                //document.documentElement!!.scrollTop = 0.toDouble() // All other browsers
                div {
                    height = 40.px
                    val dateOptionList = state.updateDates.map { Pair(it, it) }
                    selectInput(
                        options = dateOptionList,
                        value = state.dateEffective
                    ) {
                        background = Background(Color.name(Col.BLUE))
                        //if (state.updateDates.size < state.integrationDates.size) Background(Color.name(Col.LIGHTGRAY))
                        //else Background(Color.name(Col.WHITE))
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
            background = Background(hex(0xffffff))

            add(HippoItemsView(ItemType.CONSUMER, "Tjänstekonsumenter"))
            add(HippoItemsView(ItemType.CONTRACT, "Tjänstekontrakt"))
            add(HippoItemsView(ItemType.PLATTFORM_CHAIN, "Tjänsteplattformar", 18))
            add(HippoItemsView(ItemType.PRODUCER, "Tjänsteproducenter"))
            add(HippoItemsView(ItemType.LOGICAL_ADDRESS, "Logiska adresser"))
        }
    }
}

class HippoItemsView(type: ItemType, heading: String, bredd: Int = 20) : VPanel() {
    init {


        //background = Background(Col.LIGHTCYAN)
        width = bredd.vw
        margin = 3.px

        val textSearchField = searchField(type)

        div {}.stateBinding(store) { state ->
            if (
                state.currentAction != HippoAction.ViewUpdated::class &&
                state.currentAction != HippoAction.FilterItems::class &&
                state.currentAction != HippoAction.SetVMax::class &&
                state.currentAction != HippoAction.ApplicationStarted::class
            ) return@stateBinding

            if (type == ItemType.CONSUMER) println("Will now render")

            // Go to to for most rendering actions
            if (state.currentAction != HippoAction.SetVMax::class) {
                document.body!!.scrollTop = 0.toDouble()
                document.documentElement!!.scrollTop = 0.toDouble() // All other browsers
            }

            div {
                // todo: Kolla om dessa kan göras till val eller definieras inom if-satsen nedan
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
                    ItemType.CONTRACT -> {
                        vList = state.vDomainsAndContracts
                        maxCounter = state.maxCounters.contracts
                        maxNoItems = state.vServiceContractsMax
                        textFilter = state.contractFilter
                    }
                    else -> println("Error in HippoItemsView class, type = $type")
                }

                // Perform the free text search filtering
                val filteredList =
                    if (textFilter.isNotEmpty())
                        vList.filter { it.searchField.contains(textFilter, true) }
                    else
                        vList

                // Count current number of items displayed. For contracts we must calculate explicitly to remove domain items
                val noFilteredItems =
                    if (type == ItemType.CONTRACT) filteredList.filter { it::class.simpleName == "ServiceContract" }.size
                    else filteredList.size

                // Render the heading and counts
                h5("<b>$heading (${noFilteredItems}/${maxCounter})</b>")
                    .apply {
                        color = Color.hex(0x227777)
                        rich = true
                    }

                // Render the list of items
                div {
                    borderLeft = Border(1.px, BorderStyle.SOLID, Color.name(Col.GRAY))
                    borderRight = Border(1.px, BorderStyle.SOLID, Color.name(Col.GRAY))
                    borderBottom = Border(1.px, BorderStyle.SOLID, Color.name(Col.GRAY))
                    if (filteredList.size < vList.size) background = Background(Color.name(Col.LIGHTGRAY))

                    filteredList
                        .subList(0, min(filteredList.size, maxNoItems))
                        .map { item ->
                            div(
                                classes = setOf("pointer"),
                                rich = true
                            ) {
                                wordBreak = WordBreak.BREAKALL

                                // Difference for contracts, domains and rest
                                if (item::class.simpleName == "ServiceContract") {
                                    +item.description
                                    if (
                                        state.isItemFiltered(ItemType.CONTRACT, item.id) &&
                                        state.vServiceContracts.size == 1
                                    ) {
                                        insertResetButton(item, ItemType.CONTRACT)
                                    } else itemSelect(item, ItemType.CONTRACT, textSearchField)

                                } else if (item::class.simpleName == "ServiceDomain") {
                                    +"<b>${item.description}</b>"
                                    borderTop = Border(1.px, BorderStyle.SOLID, Color.name(Col.GRAY))

                                    if (
                                        state.isItemFiltered(ItemType.DOMAIN, item.id) &&
                                        state.vServiceDomains.size == 1
                                    ) {
                                        insertResetButton(item, ItemType.DOMAIN)
                                    } else itemSelect(item, ItemType.DOMAIN, textSearchField)

                                } else {
                                    val itemText =
                                        if (type == ItemType.PLATTFORM_CHAIN) item.name
                                        else "<i>${item.description}</i><br>${item.name}"

                                    +itemText
                                    borderTop = Border(1.px, BorderStyle.SOLID, Color.name(Col.GRAY))

                                    if (
                                        state.isItemFiltered(type, item.id) &&
                                        vList.size == 1
                                    ) {
                                        insertResetButton(item, type)
                                    } else itemSelect(item, type, textSearchField)
                                }
                            }
                        }
                }
                showMoreItemsButton(type, filteredList.size, maxNoItems)

            }
        }
    }
}

private fun Container.searchField(type: ItemType): TextInput {
    return textInput(type = TextInputType.SEARCH) {
        when (type) {
            ItemType.CONSUMER -> placeholder = "Sök tjänstekonsument..."
            ItemType.CONTRACT -> placeholder = "Sök tjänstekontrakt/domän..."
            ItemType.LOGICAL_ADDRESS -> placeholder = "Sök logisk adress..."
            ItemType.PRODUCER -> placeholder = "Sök tjänsteproducent..."
            ItemType.PLATTFORM_CHAIN -> placeholder = "Sök tjänsteplattform(ar)..."
            else -> placeholder == "Internal error in searchField()"
        }
        id = "searchField_${type}"
        println("Search field: $name")
        onEvent {
            var timeout = 0
            input = {
                window.clearTimeout(timeout)
                val value = self.value ?: ""
                timeout = window.setTimeout({
                    store.dispatch { dispatch, getState ->
                        if (value.isNotEmpty()) self.background = Background(Color.name(Col.LIGHTGRAY))
                        else self.background = Background(Color.name(Col.WHITE))
                        dispatch(HippoAction.FilterItems(type, value))
                    }
                }, 500)
            }
        }
    }
}

private fun Div.itemSelect(item: BaseItem, type: ItemType, searchField: TextInput) {
    onEvent {
        click = {
            // Lets begin with clearing the search field for this type
            // Hotfix 7.0.8 clear search field when item is selected
            searchField.value = null
            searchField.background = Background(Color.name(Col.WHITE))

            store.dispatch { _, getState ->
                // Hotfix 7.0.8 clear search field when item is selected
                store.dispatch(HippoAction.FilterItems(type, "")) // v7.0.8
                store.dispatch(HippoAction.ItemSelected(type, item))
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
            background = Background(Color.name(Col.GRAY))
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
                background = Background(Color.name(Col.GRAY))
                onClick {
                    store.dispatch { dispatch, getState ->
                        dispatch(HippoAction.SetVMax(type, actualLinesToShow))
                        //createViewData(getState())

                    }
                }
            }
        }
    }
}