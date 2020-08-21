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

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.core.Color.Companion.hex
import pl.treksoft.kvision.data.BaseDataComponent
import pl.treksoft.kvision.data.dataContainer
import pl.treksoft.kvision.form.select.simpleSelectInput
import pl.treksoft.kvision.form.text.TextInput
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.form.text.textInput
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.modal.Modal
import pl.treksoft.kvision.modal.ModalSize
import pl.treksoft.kvision.panel.*
import pl.treksoft.kvision.state.bind
import pl.treksoft.kvision.state.observableListOf
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.utils.vw
import se.skoview.app.store
import se.skoview.data.*
import se.skoview.lib.getVersion
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.min

class TextSearchInfo : BaseDataComponent() {
    var widgetTextSearchField = TextInput()
    val textSearchFilterObsList = observableListOf("")

    fun set(characters: String) {
        if (characters.isEmpty()) clear()
        else {
            textSearchFilterObsList[0] = characters
            widgetTextSearchField.value = characters
            widgetTextSearchField.background = Background(Color.name(Col.LIGHTGRAY))
        }
    }

    fun clear() {
        widgetTextSearchField.background = Background(Color.name(Col.WHITE))
        widgetTextSearchField.value = null
        textSearchFilterObsList[0] = ""
    }
}

object HippoTablePage : SimplePanel() {

    init {

        // font-family: Georgia,Times New Roman,Times,serif;
        fontFamily = "Times New Roman"
        // Page header
        vPanel {
            //marginRight = 5.px
            div {
                h2("Hippo - integrationer via tjänsteplattform/ar för nationell e-hälsa")
                div("Integrationer för tjänsteplattformar vars tjänstadresseringskatalog (TAK) är tillgänglig i Ineras TAK-api visas.")
            }.apply {
                //width = 100.perc
                background = Background(hex(0x113d3d))
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
            margin = 5.px
            background = Background(hex(0xf6efe9))

            // Select date

            div {
                align = Align.LEFT
            }.bind(store) { state ->
                simpleSelectInput(
                    options = state.updateDates.sortedByDescending { it }.map { Pair(it, it) },
                    value = state.dateEffective
                ) {
                    addCssStyle(formControlXs)
                    background = Background(Color.name(Col.WHITE))
                }.onEvent {
                    change = {
                        store.dispatch { dispatch, getState ->
                            //dispatch(HippoAction.DateSelected(DateType.EFFECTIaE, self.value ?: ""))
                            dispatch(HippoAction.DateSelected(DateType.EFFECTIVE_AND_END, self.value ?: ""))
                            loadIntegrations(getState())
                        }
                    }
                }
            }

            // Statistics button
            div {
                align = Align.CENTER
                //background = Background(Col.LIGHTSTEELBLUE)
            }.bind(store) { state ->
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
                val modal = Modal("Om Hippo")
                modal.iframe(src = "about.html", iframeHeight = 400, iframeWidth = 700)
                modal.size = ModalSize.LARGE
                //modal.add(H(require("img/dog.jpg")))
                modal.addButton(Button("Stäng").onClick {
                    modal.hide()
                })
                button("Om Hippo ${getVersion("hippoVersion")}", style = ButtonStyle.INFO).onClick {
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
        //flexPanel {
            //margin = 5.px

            //position = Position.ABSOLUTE
            //width = 95.vw
            overflow = Overflow.HIDDEN

            //marginRight = 24.px

            //background = Background(hex(0xff0000))

            add(HippoItemsView(ItemType.CONSUMER, "Tjänstekonsumenter", 21))//, grow = 1)
            add(HippoItemsView(ItemType.CONTRACT, "Tjänstekontrakt", 21))//, grow = 1)
            add(HippoItemsView(ItemType.PLATTFORM_CHAIN, "Tjänsteplattformar", 16))//, grow = 1)
            add(HippoItemsView(ItemType.PRODUCER, "Tjänsteproducenter", 21))//, grow = 1)
            add(HippoItemsView(ItemType.LOGICAL_ADDRESS, "Logiska adresser", 21))//, grow = 1)
        }
    }
}

class HippoItemsView(type: ItemType, heading: String, bredd: Int = 20) : VPanel() {
    init {
        //background = Background((hex(0x0fffff)))
        width = (bredd - 0.5).vw
        overflow = Overflow.HIDDEN
        margin = (0.3).vw
        wordBreak = WordBreak.BREAKALL

        // Render the search field
        val textSearchInfo = TextSearchInfo()
        searchField(type, textSearchInfo)

        div {}.bind(store) { state ->
            /*
            if (
                state.currentAction != HippoAction.ViewUpdated::class &&
                state.currentAction != HippoAction.SetVMax::class &&
                state.currentAction != HippoAction.ApplicationStarted::class
            ) return@bind
             */

            //if (type == ItemType.CONSUMER)
            println("Will now render ${type}")

            // Go to top for most rendering actions
            if (state.currentAction != HippoAction.SetVMax::class) {
                document.body!!.scrollTop = 0.toDouble()
                document.documentElement!!.scrollTop = 0.toDouble() // All other browsers
            }

            // todo: Kolla om dessa kan göras till val eller definieras inom if-satsen nedan
            var vList = listOf<BaseItem>()
            var maxCounter = -1
            var maxNoItems = -1

            //background = Background(Col.LIGHTSTEELBLUE)
            when (type) {
                ItemType.CONSUMER -> {
                    vList = state.vServiceConsumers
                    maxCounter = state.maxCounters.consumers
                    maxNoItems = state.vServiceConsumersMax
                }
                ItemType.PRODUCER -> {
                    vList = state.vServiceProducers
                    maxCounter = state.maxCounters.producers
                    maxNoItems = state.vServiceProducersMax
                }
                ItemType.PLATTFORM_CHAIN -> {
                    vList = state.vPlattformChains
                    maxCounter = state.maxCounters.plattformChains
                    maxNoItems = 100
                }
                ItemType.LOGICAL_ADDRESS -> {
                    vList = state.vLogicalAddresses
                    maxCounter = state.maxCounters.logicalAddress
                    maxNoItems = state.vLogicalAddressesMax
                    marginRight = 10.px
                }
                ItemType.CONTRACT -> {
                    vList = state.vDomainsAndContracts
                    maxCounter = state.maxCounters.contracts
                    maxNoItems = state.vServiceContractsMax
                }
                else -> println("Error in HippoItemsView class, type = $type")
            }

            dataContainer(textSearchInfo.textSearchFilterObsList, { textFilter, _, _ ->
                println("In dataContainer, type = $type, textFilter = '$textFilter'")
                //textFilter = element

                div {
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
                            color = hex(0x227777)
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
                                    margin = 5.px
                                    //wordBreak = WordBreak.BREAKALL
                                    margin = 5.px

                                    // Difference for contracts, domains and rest
                                    if (item::class.simpleName == "ServiceContract") {
                                        +item.description
                                        if (
                                            state.isItemSelected(ItemType.CONTRACT, item.id) &&
                                            state.vServiceContracts.size == 1
                                        ) {
                                            insertResetButton(item, ItemType.CONTRACT)
                                        } else itemSelect(item, ItemType.CONTRACT, textSearchInfo)

                                    } else if (item::class.simpleName == "ServiceDomain") {
                                        +"<b>${item.description}</b>"
                                        borderTop = Border(1.px, BorderStyle.SOLID, Color.name(Col.GRAY))

                                        if (
                                            state.isItemSelected(ItemType.DOMAIN, item.id) &&
                                            state.vServiceDomains.size == 1
                                        ) {
                                            insertResetButton(item, ItemType.DOMAIN)
                                        } else itemSelect(item, ItemType.DOMAIN, textSearchInfo)

                                    } else {
                                        val itemText =
                                            if (type == ItemType.PLATTFORM_CHAIN) item.name
                                            else "<i>${item.description}</i><br>${item.name}"

                                        +itemText
                                        borderTop = Border(1.px, BorderStyle.SOLID, Color.name(Col.GRAY))

                                        if (
                                            state.isItemSelected(type, item.id) &&
                                            vList.size == 1
                                        ) {
                                            insertResetButton(item, type)
                                        } else itemSelect(item, type, textSearchInfo)
                                    }
                                }
                            }
                    }
                    showMoreItemsButton(type, filteredList.size, maxNoItems)

                }
            }) // end of dataContainer
        }
    }
}

private fun Container.searchField(type: ItemType, textSearchInfo: TextSearchInfo) {
    textSearchInfo.widgetTextSearchField = textInput(type = TextInputType.SEARCH) {
        when (type) {
            ItemType.CONSUMER -> placeholder = "Sök tjänstekonsument..."
            ItemType.CONTRACT -> placeholder = "Sök tjänstekontrakt/domän..."
            ItemType.LOGICAL_ADDRESS -> {
                //marginRight = 20.px
                placeholder = "Sök logisk adress..."
            }
            ItemType.PRODUCER -> placeholder = "Sök tjänsteproducent..."
            ItemType.PLATTFORM_CHAIN -> placeholder = "Sök tjänsteplattform(ar)..."
            else -> placeholder == "Internal error in searchField()"
        }
        onEvent {
            var timeout = 0
            input = {
                window.clearTimeout(timeout)
                val value = self.value ?: ""

                timeout = window.setTimeout({
                    textSearchInfo.set(value)
                }, 300)

            }
        }
    }
}

private fun Div.itemSelect(item: BaseItem, type: ItemType, textSearchInfo: TextSearchInfo) {
    onEvent {
        click = {
            textSearchInfo.clear()

            store.dispatch { _, getState ->
                store.dispatch(HippoAction.ItemIdSelected(type, item.id))
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
            marginTop = 20.px
            width = 100.perc
            background = Background(Color.name(Col.GRAY))
            onClick {
                //store.dispatch { dispatch, getState ->
                    store.dispatch(HippoAction.ItemIdDeselected(type, item.id))
                    //createViewData(getState())
                    loadIntegrations(store.getState())
                //}
            }
        }
    }
}

private fun Container.showMoreItemsButton(type: ItemType, size: Int, maxItemsToShow: Int) {
    if (size > maxItemsToShow) {
        div {
            val linesLeft = size - maxItemsToShow
            val moreLinesToShow = min(linesLeft, 500)
            val actualLinesToShow = moreLinesToShow + maxItemsToShow
            button("Visa ytterligare ${moreLinesToShow} rader", style = ButtonStyle.PRIMARY) {
                width = 100.perc
                background = Background(Color.name(Col.GRAY))
                onClick {
                    //store.dispatch { dispatch, _ ->
                        store.dispatch(HippoAction.SetVMax(type, actualLinesToShow))

                    //}
                }
            }
        }
    }
}