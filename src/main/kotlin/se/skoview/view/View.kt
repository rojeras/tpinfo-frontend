package se.skoview.view

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.form.InputSize
import pl.treksoft.kvision.form.select.selectInput
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.html.Align
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.state.stateBinding
import pl.treksoft.kvision.table.*
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px
import se.skoview.data.*

data class ViewInformation(
    val baseItem: BaseItem,
    val showData: String,
    val type: ItemType
)

object hippoPage : SimplePanel() {

    init {

        println("In hippoPage")

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
            div(classes = setOf(""""class="cssload-loader""""))
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
                            store.dispatch(HippoAction.DateSelected(self.value ?: ""))
                            store.dispatch { dispatch, getState ->
                                dispatch(HippoAction.DateSelected(self.value ?: ""))
                                loadIntegrations(getState())
                            }
                        }
                    }
            }
        }
        vPanel {
        }.apply {
            //background = Background(0x009090)
            width = 100.perc
        }.stateBinding(store) { state ->
            if (state.showIntegrations) {
                println("===== View invoked =====")

                val integrationLists = createViewData(state)

                //println("========================> Actions submitter: ${state::class.simpleName}")
                val consumerHeading: String =
                    "Tjänstekonsumenter (${integrationLists.serviceConsumers.size}/${state.maxCounters.consumers})"
                val contractHeading: String =
                    "Tjänstekontrakt (${integrationLists.serviceContracts.size}/${state.maxCounters.contracts})"
                val plattformHeading: String =
                    "Tjänsteplattformar (${integrationLists.plattformChains.size}/${state.maxCounters.plattformChains})"
                val logicalAddressHeading: String =
                    "Logiska adresser (${integrationLists.logicalAddresses.size}/${state.maxCounters.logicalAddress})"
                val producerHeading: String =
                    "Tjänsteproducenter (${integrationLists.serviceProducers.size}/${state.maxCounters.producers})"

                val viewConsumerLst: MutableList<ViewInformation> = mutableListOf()
                integrationLists.serviceConsumers.map {
                    viewConsumerLst.add(
                        ViewInformation(
                            it,
                            "<i>${it.description}</i><br>${it.hsaId}",
                            ItemType.CONSUMER
                        )
                    )
                }

                val viewContractLst: MutableList<ViewInformation> = mutableListOf()
                integrationLists.domainsAndContracts.map {
                    if (it::class.simpleName == "ServiceDomain") {
                        val desc = "<b>${it.description}</b>"
                        viewContractLst.add(ViewInformation(it, desc, ItemType.DOMAIN))
                    } else {
                        val desc = it.description
                        viewContractLst.add(ViewInformation(it, desc, ItemType.CONTRACT))
                    }
                }

                val viewPlattformList: MutableList<ViewInformation> = mutableListOf()
                integrationLists.plattformChains.map {
                    viewPlattformList.add(
                        ViewInformation(
                            it,
                            it.name,
                            ItemType.PLATTFORM_CHAIN
                        )
                    )
                }

                val viewLogicalAddressList: MutableList<ViewInformation> = mutableListOf()
                integrationLists.logicalAddresses.map {
                    viewLogicalAddressList.add(
                        ViewInformation(
                            it,
                            "<i>${it.description}</i><br>${it.name}",
                            ItemType.LOGICAL_ADDRESS
                        )
                    )
                }

                val viewProducerLst: MutableList<ViewInformation> = mutableListOf()
                integrationLists.serviceProducers.map {
                    viewProducerLst.add(
                        ViewInformation(
                            it,
                            "<i>${it.description}</i><br>${it.hsaId}",
                            ItemType.PRODUCER
                        )
                    )
                }

                add(
                    //HippoTabulatorPage(
                    HippoTablePage(
                        consumerHeading,
                        contractHeading,
                        plattformHeading,
                        logicalAddressHeading,
                        producerHeading,
                        viewConsumerLst,
                        viewContractLst,
                        viewPlattformList,
                        viewLogicalAddressList,
                        viewProducerLst
                    )
                )
            }
        }
    }
}

