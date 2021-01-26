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
package se.skoview.stat

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.form.check.checkBoxInput
import pl.treksoft.kvision.form.select.simpleSelectInput
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.modal.Modal
import pl.treksoft.kvision.modal.ModalSize
import pl.treksoft.kvision.panel.flexPanel
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.table.TableType
import pl.treksoft.kvision.table.cell
import pl.treksoft.kvision.table.row
import pl.treksoft.kvision.table.table
import pl.treksoft.kvision.utils.px
import se.skoview.app.formControlXs
import se.skoview.common.*

var statPageTop: Div = Div()

fun Container.statHeader(state: HippoState) {
    println("In statHeader()")
    div {
        fontFamily = "Times New Roman"
        id = "StatPage:SimplePanel()"
        this.marginTop = 10.px

        statPageTop = div {
            // }.bind(store) { state ->
            id = "StatPageTop"

            // Page header
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
                // }.bind(store) { state ->
                spacing = 5
                clear = Clear.BOTH
                margin = 0.px
                background = Background(Color.hex(0xf6efe9))
                id = "StatPage-ControlPanel:FlexPanel-Bind"
                table(
                    listOf(),
                    setOf(TableType.BORDERED, TableType.SMALL)
                ) {
                    id = "ControlPanel-Table"
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
                content = headingText
                align = Align.CENTER
                fontWeight = FontWeight.BOLD
            }

            // showHistoryChart(state)
        }

        // pieView(state)
    }
}
