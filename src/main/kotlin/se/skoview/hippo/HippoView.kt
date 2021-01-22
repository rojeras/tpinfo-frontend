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
package se.skoview.hippo

import kotlinx.browser.document
import kotlinx.browser.window
import pl.treksoft.kvision.core.* // ktlint-disable no-wildcard-imports
import pl.treksoft.kvision.core.Color.Companion.hex
import pl.treksoft.kvision.data.BaseDataComponent
import pl.treksoft.kvision.data.dataContainer
import pl.treksoft.kvision.form.select.simpleSelectInput
import pl.treksoft.kvision.form.text.TextInput
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.form.text.textInput
import pl.treksoft.kvision.html.* // ktlint-disable no-wildcard-imports
import pl.treksoft.kvision.modal.Modal
import pl.treksoft.kvision.modal.ModalSize
import pl.treksoft.kvision.panel.flexPanel
import pl.treksoft.kvision.panel.hPanel
import pl.treksoft.kvision.panel.simplePanel
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.state.observableListOf
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.utils.vw
import se.skoview.app.formControlXs
import se.skoview.common.* // ktlint-disable no-wildcard-imports
import kotlin.math.min

fun Container.hippoView(state: HippoState) {
    println("In hippoView")
    val integrationLists = createHippoViewData(state)
    simplePanel {
        // font-family: Georgia,Times New Roman,Times,serif;
        fontFamily = "Times New Roman"
        width = 99.vw
        // Page header
        vPanel {
            // marginRight = 5.px
            div {
                h2("Hippo - integrationer via tjänsteplattform/ar för nationell e-hälsa")
                div("Integrationer för tjänsteplattformar vars tjänstadresseringskatalog (TAK) är tillgänglig i Ineras TAK-api visas.")
            }.apply {
                // width = 100.perc
                background = Background(hex(0x113d3d))
                align = Align.CENTER
                color = Color.name(Col.WHITE)
                marginTop = 5.px
            }
        }

        // Date selector
        flexPanel(
            // FlexDir.ROW, FlexWrap.WRAP, FlexJustify.SPACEBETWEEN, FlexAlignItems.CENTER, // Before upgrading to 3.13.1
            FlexDirection.ROW, FlexWrap.WRAP, JustifyContent.SPACEBETWEEN, AlignItems.CENTER,
            spacing = 5
        ) {
            clear = Clear.BOTH
            margin = 5.px
            background = Background(hex(0xf6efe9))

            // Select date
            div {
                align = Align.LEFT
//             }.bind(HippoManager.hippoStore) { state ->
                simpleSelectInput(
                    options = state.updateDates.sortedByDescending { it }.map { Pair(it, it) },
                    value = state.dateEffective
                ) {
                    addCssStyle(formControlXs)
                    width = 80.px
                    background = Background(Color.name(Col.WHITE))
                }.onEvent {
                    change = {
                        // store.dispatch { dispatch, getState ->
                        val date = self.value ?: ""
                        // HippoManager.bothDatesSelected(date)
                        HippoManager.dateSelected(DateType.EFFECTIVE_AND_END, date)
                        // }
                    }
                }
            }

            // Statistics button
            div {
                align = Align.CENTER
                //       }.bind(HippoManager.hippoStore) { state ->
                val chainId =
                    if (integrationLists.plattformChains.size == 1) integrationLists.plattformChains[0].id
                    else -1

                var disabled: Boolean = true
                if (chainId > 0) {
                    val chain = PlattformChain.mapp[chainId]

                    disabled = !(
                            StatisticsPlattform.mapp.containsKey(chain!!.first) ||
                                    StatisticsPlattform.mapp.containsKey(chain.last)
                            )
                }
                val buttonStyle: ButtonStyle =
                    if (disabled) ButtonStyle.LIGHT
                    else ButtonStyle.INFO
                button(
                    "Visa statistik",
                    style = buttonStyle,
                    disabled = disabled
                ) {
                    size = ButtonSize.SMALL
                }.onClick {
                    // HippoManager.setView(View.STAT_ADVANCED)
                    HippoManager.setView(View.STAT)
                }.apply {
                    // addBsBgColor(BsBgColor.LIGHT)
                    // addBsColor(BsColor.BLACK50)
                }
            }

            // About button
            div {
                align = Align.RIGHT
                val modal = Modal("Om Hippo")
                modal.iframe(src = "about.html", iframeHeight = 400, iframeWidth = 700)
                modal.size = ModalSize.LARGE
                modal.addButton(
                    Button(
                        "Stäng"
                    ).onClick {
                        modal.hide()
                    }
                )
                button(
                    "Om Hippo ${getVersion("hippoVersion")}",
                    style = ButtonStyle.INFO
                ) {
                    size = ButtonSize.SMALL
                }.onClick {
                    modal.show()
                }.apply {
                    addBsBgColor(BsBgColor.LIGHT)
                    addBsColor(BsColor.BLACK50)
                }
            }
        }

// The whole item table
        hPanel {
            overflow = Overflow.HIDDEN

            hippoItemsView(state, ItemType.CONSUMER, integrationLists, "Tjänstekonsumenter", 21) // , grow = 1)
            hippoItemsView(state, ItemType.CONTRACT, integrationLists, "Tjänstekontrakt", 21) // , grow = 1)
            hippoItemsView(state, ItemType.PLATTFORM_CHAIN, integrationLists, "Tjänsteplattformar", 16) // , grow = 1)
            hippoItemsView(state, ItemType.PRODUCER, integrationLists, "Tjänsteproducenter", 21) // , grow = 1)
            hippoItemsView(state, ItemType.LOGICAL_ADDRESS, integrationLists, "Logiska adresser", 21) // , grow = 1)
        }
    }
}

private fun Container.hippoItemsView(
    state: HippoState,
    type: ItemType,
    integrationLists: IntegrationLists,
    heading: String,
    bredd: Int = 20
) {
    div {
        width = (bredd - 0.5).vw
        overflow = Overflow.HIDDEN
        margin = (0.3).vw
        wordBreak = WordBreak.BREAKALL

        val textSearchInfo = TextSearchInfo()
        searchField(type, textSearchInfo)

        // Go to top for most rendering actions
        if (state.currentAction != HippoAction.SetVMax::class) {
            document.body!!.scrollTop = 0.toDouble()
            document.documentElement!!.scrollTop = 0.toDouble() // All other browsers
        }

        // todo: Kolla om dessa kan göras till val eller definieras inom if-satsen nedan
        var vList = listOf<BaseItem>()
        var maxCounter = -1
        var maxNoItems = -1

        when (type) {
            ItemType.CONSUMER -> {
                // vList = state.vServiceConsumers
                vList = integrationLists.serviceConsumers
                maxCounter = state.maxCounters.consumers
                maxNoItems = state.vServiceConsumersMax
            }
            ItemType.PRODUCER -> {
                // vList = state.vServiceProducers
                vList = integrationLists.serviceProducers
                maxCounter = state.maxCounters.producers
                maxNoItems = state.vServiceProducersMax
            }
            ItemType.PLATTFORM_CHAIN -> {
                // vList = state.vPlattformChains
                vList = integrationLists.plattformChains
                maxCounter = state.maxCounters.plattformChains
                maxNoItems = 100
            }
            ItemType.LOGICAL_ADDRESS -> {
                // vList = state.vLogicalAddresses
                vList = integrationLists.logicalAddresses
                maxCounter = state.maxCounters.logicalAddress
                maxNoItems = state.vLogicalAddressesMax
                marginRight = 10.px
            }
            ItemType.CONTRACT -> {
                // vList = state.vDomainsAndContracts
                vList = integrationLists.domainsAndContracts
                maxCounter = state.maxCounters.contracts
                maxNoItems = state.vServiceContractsMax
            }
            else -> println("Error in HippoItemsView class, type = $type")
        }

        dataContainer(
            textSearchInfo.textSearchFilterObsList,
            { textFilter, _, _ ->

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
                    h5("<b>$heading ($noFilteredItems/$maxCounter)</b>")
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
                                    margin = 5.px

                                    // Difference for contracts, domains and rest
                                    if (item::class.simpleName == "ServiceContract") {
                                        +item.description
                                        if (
                                            state.isItemSelected(
                                                ItemType.CONTRACT,
                                                item.id
                                            ) // && integrationLists.serviceContracts.size == 1
                                        ) {
                                            insertResetButton(item, ItemType.CONTRACT)
                                        } else itemSelect(item, ItemType.CONTRACT, textSearchInfo)
                                    } else if (item::class.simpleName == "ServiceDomain") {
                                        +"<b>${item.description}</b>"
                                        borderTop = Border(1.px, BorderStyle.SOLID, Color.name(Col.GRAY))

                                        if (
                                            state.isItemSelected(
                                                ItemType.DOMAIN,
                                                item.id
                                            ) // && integrationLists.serviceDomains.size == 1
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
                                            state.isItemSelected(type, item.id) // && vList.size == 1
                                        ) {
                                            insertResetButton(item, type)
                                        } else itemSelect(item, type, textSearchInfo)
                                    }
                                }
                            }
                    }
                    if (state.downloadIntegrationStatus == AsyncActionStatus.COMPLETED)
                        showMoreItemsButton(type, filteredList.size, maxNoItems)
                }
            }
        ) // end of dataContainer
    }
}

private fun Container.searchField(type: ItemType, textSearchInfo: TextSearchInfo) {
    textSearchInfo.widgetTextSearchField = textInput(type = TextInputType.SEARCH) {
        when (type) {
            ItemType.CONSUMER -> placeholder = "Sök tjänstekonsument..."
            ItemType.CONTRACT -> placeholder = "Sök tjänstekontrakt/domän..."
            ItemType.LOGICAL_ADDRESS -> {
                // marginRight = 20.px
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

                timeout = window.setTimeout(
                    {
                        textSearchInfo.set(value)
                    },
                    300
                )
            }
        }
    }
}

private fun Div.itemSelect(item: BaseItem, type: ItemType, textSearchInfo: TextSearchInfo) {
    onEvent {
        click = {
            textSearchInfo.clear()
            HippoManager.itemSelected(item.id, type)
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
        button(
            buttonText,
            style = ButtonStyle.PRIMARY
        ) {
            size = ButtonSize.SMALL
            marginTop = 20.px
            width = 100.perc
            background = Background(Color.name(Col.GRAY))
        }.onClick {
            HippoManager.itemDeselected(item.id, type)
        }
    }
}

private fun Container.showMoreItemsButton(type: ItemType, size: Int, maxItemsToShow: Int) {
    if (size > maxItemsToShow) {
        div {
            val linesLeft = size - maxItemsToShow
            val moreLinesToShow = min(linesLeft, 500)
            val actualLinesToShow = moreLinesToShow + maxItemsToShow
            button("Visa ytterligare $moreLinesToShow rader", style = ButtonStyle.PRIMARY) {
                width = 100.perc
                background = Background(Color.name(Col.GRAY))
                onClick {
                    HippoManager.setViewMax(type, actualLinesToShow)
                }
            }
        }
    }
}

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
