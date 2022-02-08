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
package se.skoview.view.hippo

import io.kvision.core.* // ktlint-disable no-wildcard-imports
import io.kvision.core.Color.Companion.hex
import io.kvision.data.BaseDataComponent
import io.kvision.data.dataContainer
import io.kvision.form.select.simpleSelectInput
import io.kvision.form.text.TextInput
import io.kvision.form.text.TextInputType
import io.kvision.form.text.textInput
import io.kvision.html.* // ktlint-disable no-wildcard-imports
import io.kvision.modal.Modal
import io.kvision.modal.ModalSize
import io.kvision.panel.flexPanel
import io.kvision.panel.hPanel
import io.kvision.panel.simplePanel
import io.kvision.panel.vPanel
import io.kvision.state.observableListOf
import io.kvision.utils.perc
import io.kvision.utils.px
import io.kvision.utils.vw
import kotlinx.browser.document
import kotlinx.browser.window
import se.skoview.controller.HippoManager
import se.skoview.controller.View
import se.skoview.controller.formControlXs
import se.skoview.controller.getVersion
import se.skoview.model.* // ktlint-disable no-wildcard-imports
import kotlin.math.min

fun Container.hippoView(state: HippoState) {
    println("In hippoView")
    val integrationLists = createHippoViewData(state)
    simplePanel {
        // font-family: Georgia,Times New Roman,Times,serif;
        fontFamily = "Times New Roman"
        // width = 99.vw
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
            FlexDirection.ROW, FlexWrap.WRAP, JustifyContent.SPACEBETWEEN, AlignItems.CENTER,
            spacing = 5
        ) {
            clear = Clear.BOTH
            margin = 5.px
            background = Background(hex(0xf6efe9))

            // Select date
            div {
                align = Align.LEFT

                val opts: List<Pair<String, String>> = listOf(
                    state.updateDates,
                    listOf(BaseDates.getLastIntegrationDate()),
                    listOf(state.dateEffective)
                ).flatten() // as MutableList<String>
                    // val opts: List<Pair<String, String>> = state.updateDates
                    .distinct()
                    .sortedByDescending { it }
                    .map { Pair(it, it) } as List<Pair<String, String>>

                simpleSelectInput(
                    options = opts,
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
                val chainId =
                    if (integrationLists.plattformChains.size == 1) integrationLists.plattformChains[0].id
                    else -1

                var disabled = true
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
                }
            }

            // About button
            div {
                align = Align.RIGHT
                val modal = Modal("Om Hippo")
                modal.iframe(src = "about-hippo.html", iframeHeight = 400, iframeWidth = 700)
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
        width = (bredd - 1.1).vw
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
                vList = integrationLists.serviceConsumers
                maxCounter = state.maxCounters.consumers
                maxNoItems = state.vServiceConsumersMax
            }
            ItemType.PRODUCER -> {
                vList = integrationLists.serviceProducers
                maxCounter = state.maxCounters.producers
                maxNoItems = state.vServiceProducersMax
            }
            ItemType.PLATTFORM_CHAIN -> {
                vList = integrationLists.plattformChains
                maxCounter = state.maxCounters.plattformChains
                maxNoItems = 100
            }
            ItemType.LOGICAL_ADDRESS -> {
                vList = integrationLists.logicalAddresses
                maxCounter = state.maxCounters.logicalAddress
                maxNoItems = state.vLogicalAddressesMax
                marginRight = 10.px
            }
            ItemType.CONTRACT -> {
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
                                    className = "pointer",
                                    rich = true
                                ) {
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

                                        enablePopover(
                                            PopoverOptions(
                                                content = """
                                                    <div>
                                                        <a href="https://rivta.se/tkview/#/domain/${item.description}" target="_blank">
                                                            <img alt="Extern dokumentation" src=/icons/external-link-30.png width=20" height="20">
                                                            <span>Extern dokumentation</span>
                                                        </a>
                                                    </div>
                                                """.trimIndent(),
                                                triggers = listOf(Trigger.HOVER),
                                                delay = 400,
                                                hideDelay = 2000,
                                                rich = true
                                            )
                                        )

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
