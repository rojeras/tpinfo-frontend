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
package se.skoview.view.stat

import com.github.snabbdom.VNode
import io.kvision.core.*
import io.kvision.form.check.checkBoxInput
import io.kvision.form.select.simpleSelectInput
import io.kvision.html.*
import io.kvision.modal.Modal
import io.kvision.modal.ModalSize
import io.kvision.panel.VPanel
import io.kvision.panel.flexPanel
import io.kvision.panel.vPanel
import io.kvision.table.TableType
import io.kvision.table.cell
import io.kvision.table.row
import io.kvision.table.table
import io.kvision.utils.px
import se.skoview.controller.HippoManager
import se.skoview.controller.View
import se.skoview.controller.formControlXs
import se.skoview.controller.showBackgroundColorsForDebug
import se.skoview.model.*

fun Container.statHeader(
    state: HippoState,
) {
    println("In statHeader()")
    // Whole header block
    // vPanel {
    add(StatHeader(state))
}

class StatHeader(state: HippoState) : VPanel() {

    init {

        fontFamily = "Times New Roman"
        id = "StatHeader"
        if (showBackgroundColorsForDebug) background = Background(Color.name(Col.LIGHTGRAY))
        overflow = Overflow.HIDDEN
        height = StatPanelSize.statHeaderHeight
        this.marginTop = 10.px

        // Page top
        div {
            h2("Antal meddelanden genom Region Stockholms tjänsteplattform")
            div("Detaljerad statistik med diagram och möjlighet att ladda ner informationen för egna analyser.")
        }.apply {
            // width = 100.perc
            id = "StatPage-HeadingArea:Div"
            background = Background(Color.hex(0x113d3d))
            align = Align.CENTER
            color = Color.name(Col.WHITE)
            marginTop = 5.px
        }

        flexPanel(
            FlexDirection.ROW,
            FlexWrap.WRAP,
            JustifyContent.SPACEBETWEEN,
            AlignItems.CENTER,
        ) {
            id = "statHeader:StatPageTop:flexPanel"
            spacing = 5
            clear = Clear.BOTH
            margin = 0.px
            background = Background(Color.hex(0xf6efe9))
            table(
                listOf(),
                setOf(TableType.BORDERLESS, TableType.SMALL)
            ) {
                id = "statHeader:StatPageTop:flexPanel:table"
                marginBottom = 0.px
                // Start date
                row {
                    id = "First row"
                    cell { +"Startdatum:" }
                    cell {
                        simpleSelectInput(
                            // options = state.statisticsDates
                            options = BaseDates.statisticsDates
                                .sortedByDescending { it }
                                .filter { it <= state.statDateEnd }
                                .map { Pair(it, it) },
                            value = state.statDateEffective,
                        ) {
                            addCssStyle(formControlXs)
                            width = 80.px
                            background = Background(Color.name(Col.WHITE))
                        }.onEvent {
                            change = {
                                HippoManager.dateSelected(DateType.STAT_EFFECTIVE, self.value ?: "")
                            }
                        }
                    }
                    cell { +"Plattform:" }
                    cell {
                        val selectedPlattformId =
                            if (state.selectedPlattformChainsIds.isNotEmpty())
                                PlattformChain.mapp[state.selectedPlattformChainsIds[0]]!!.last.toString()
                            else ""

                        val options = StatisticsPlattform.mapp.map { Pair(it.key.toString(), it.value.name) }
                        simpleSelectInput(
                            options = options,
                            value = selectedPlattformId
                        ) {
                            addCssStyle(formControlXs)
                            background = Background(Color.name(Col.WHITE))
                        }.onEvent {
                            change = {
                                val selectedTp = (self.value ?: "").toInt()
                                HippoManager.statTpSelected(selectedTp)
                            }
                        }
                    }

                    // Show time graph
                    cell {
                        checkBoxInput(
                            value = state.showTimeGraph
                        ).onClick {
                            HippoManager.statHistorySelected(value)
                        }
                        +" Visa utveckling över tid"
                    }

                    cell {

                        button(
                            "Se urval i hippo",
                            style = ButtonStyle.INFO,
                        ) {
                            size = ButtonSize.SMALL
                        }.onClick {
                            HippoManager.setView(View.HIPPO)
                        }.apply {
                            addBsBgColor(BsBgColor.LIGHT)
                            addBsColor(BsColor.BLACK50)
                        }
                    }
                }

                // End date
                row {
                    id = "Second row"
                    cell { +"Slutdatum:" }
                    cell {
                        simpleSelectInput(
                            options = BaseDates.statisticsDates
                                .sortedByDescending { it }
                                .filter { it >= state.statDateEffective }
                                .map { Pair(it, it) },
                            value = state.statDateEnd
                        ) {
                            addCssStyle(formControlXs)
                            background = Background(Color.name(Col.WHITE))
                        }.onEvent {
                            change = {
                                HippoManager.dateSelected(DateType.STAT_END, self.value ?: "")
                                // }
                            }
                        }
                    }

                    cell { +"Visa:" }
                    cell {
                        val selectedPreSelectLabel: String =
                            if (state.viewPreSelect == null) ""
                            else state.viewPreSelect.label

                        val options: List<Pair<String, String>> =
                            listOf(Pair("", "Allt")) +
                                PreSelect.mapp
                                    .toList()
                                    .sortedBy { it.first }
                                    .map { Pair(it.first, it.first) }

                        simpleSelectInput(
                            options = options,
                            value = selectedPreSelectLabel
                        ) {
                            addCssStyle(formControlXs)
                            background = Background(Color.name(Col.WHITE))
                        }.onEvent {
                            change = {
                                val preSelectLabel: String = self.value ?: "dummy"
                                HippoManager.statSetPreselect(preSelectLabel)
                            }
                        }
                    }

                    cell {
                        checkBoxInput(
                            value = state.showTechnicalTerms
                        ).onClick {
                            HippoManager.statTechnicalTermsSelected(value)
                        }
                        +" Tekniska termer"
                    }
                    cell {

                        val disabled: Boolean = (
                            state.showConsumers &&
                                state.showProducers &&
                                state.showContracts &&
                                state.showLogicalAddresses
                            )

                        button(
                            "Visa samtliga",
                            style = ButtonStyle.INFO,
                            disabled = disabled
                        ) {
                            size = ButtonSize.SMALL
                        }.onClick {
                            HippoManager.statShowAllItemTypes()
                        }.apply {
                            addBsBgColor(BsBgColor.LIGHT)
                            addBsColor(BsColor.BLACK50)
                        }
                    }
                }
            }

            // About button
            vPanel(
                spacing = 2,
            ) {
                marginRight = 17.px

                button(
                    "Exportera",
                    style = ButtonStyle.INFO,
                ) {
                    size = ButtonSize.SMALL
                }.onClick {
                    exportStatData(state)
                }.apply {
                    addBsBgColor(BsBgColor.LIGHT)
                    addBsColor(BsColor.BLACK50)
                }

                val modal = Modal("Om Statistikfunktionen")
                modal.iframe(src = "about-stat.html", iframeHeight = 400, iframeWidth = 700)
                modal.size = ModalSize.LARGE
                // modal.add(H(require("img/dog.jpg")))
                modal.addButton(
                    Button("Stäng").onClick {
                        modal.hide()
                    }
                )
                button(
                    "Om Statistik ${getVersion("hippoVersion")}",
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

        // Heading
        val tCalls: String = state.statBlob.callsDomain.map { it.value }.sum().toString().thousands()

        val headingText: String =
            if (state.viewPreSelect == null) "Totalt antal anrop för detta urval är: $tCalls"
            else "${state.viewPreSelect.label}: $tCalls anrop"

        h4 {
            id = "pageHeading"
            content = headingText
            align = Align.CENTER
            fontWeight = FontWeight.BOLD
        }
    }

    override fun afterInsert(node: VNode) {
        super.afterInsert(node)
        val height = this.getElementJQuery()?.height()
        if (height != null) {
            StatPanelSize.statHeaderHeightPx = height.toInt()
            println("offset is:")
            console.log(this.getElementJQuery()!!.offset())
        }
    }
}
