package se.skoview.view

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.form.select.selectInput
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.html.Align
import pl.treksoft.kvision.i18n.I18n
import pl.treksoft.kvision.panel.FlexAlignItems
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.root
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.state.ObservableList
import pl.treksoft.kvision.state.observableListOf
import pl.treksoft.kvision.state.stateBinding
import pl.treksoft.kvision.table.*
import pl.treksoft.kvision.tabulator.*
import pl.treksoft.kvision.utils.auto
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px
import se.skoview.data.*
import se.skoview.lib.toSwedishDate

data class ViewInformation(
    val baseItem: BaseItem,
    var showData: String
)

object hippoPage : SimplePanel() {

    init {
        println("In hippoPage")
        //vPanel(alignItems = FlexAlignItems.STRETCH) { this.width = 100.perc
        //searchField()

        vPanel(alignItems = FlexAlignItems.STRETCH) {
            //maxWidth = 1200.px
            textAlign = TextAlign.CENTER
            marginLeft = auto
            marginRight = auto
        }.stateBinding(store) { state ->
            informationText(state)
            if (!state.downloadingBaseItems && state.errorMessage == null) {
                //pokemonGrid(state)
                //pagination(state)
            }
        }
        //}
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
            }
        }
        vPanel {
        }.apply {
            clear = Clear.BOTH
            margin = 0.px
            background = Background(0xf6efe9)
        }.stateBinding(store) { state ->
            div {
                //add(DateSelectPanel(state.updateDates, state.dateEffective.toSwedishDate()))
                val dateOptionList = state.updateDates.map { Pair(it, it) }
                selectInput(
                    options = dateOptionList,
                    value = state.dateEffective
                ).onEvent {
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
        }.stateBinding(store) { state ->
            val consumerHeading: String =
                "Tjänstekonsumenter (${state.vServiceConsumers.size}/${state.maxCounters.consumers})"
            val contractHeading: String =
                "Tjänstekontrakt (${state.vServiceContracts.size}/${state.maxCounters.contracts})"
            val plattformHeading: String =
                "Tjänsteplattformar (${state.vPlattformChains.size}/${state.maxCounters.plattformChains})"
            val logicalAddressHeading: String =
                "Logiska adresser (${state.vLogicalAddresses.size}/${state.maxCounters.logicalAddress})"
            val producerHeading: String =
                "Tjänsteproducenter (${state.vServiceProducers.size}/${state.maxCounters.producers})"
            table(
                listOf(
                    consumerHeading,
                    contractHeading,
                    plattformHeading,
                    logicalAddressHeading,
                    producerHeading
                ),
                setOf(TableType.SMALL)
                //responsiveType = ResponsiveType.RESPONSIVE
            )
            {
                row {
                    cell { +"Searchbox" }.apply { width = 20.perc }
                    cell { +"Searchbox" }.apply { width = 20.perc }
                    cell { +"Searchbox" }.apply { width = 20.perc }
                    cell { +"Searchbox" }.apply { width = 20.perc }
                    cell { +"Searchbox" }.apply { width = 20.perc }
                }
                row {
                    // Service consumers
                    cell {
                        //var obsConsumer: ObservableList<ViewInformation> = observableListOf()
                        val viewConsumerLst: MutableList<ViewInformation> = mutableListOf()
                        state.vServiceConsumers.map { viewConsumerLst.add(ViewInformation(it, "<i>${it.description}</i><br>${it.hsaId}")) }
                        add(HippoTabulator(consumerHeading, viewConsumerLst))
                    }
                    // Service contracts
                    cell {
                        val viewContractLst: MutableList<ViewInformation> = mutableListOf()
                        state.vDomainsAndContracts.map { viewContractLst.add(ViewInformation(it, "<i>${it.description}</i><br>${it.name}")) }
                        add(HippoTabulator(contractHeading, viewContractLst))
                    }
                    // Plattforms
                    cell {
                        val viewPlattformList: MutableList<ViewInformation> = mutableListOf()
                        state.vPlattformChains.map { viewPlattformList.add(ViewInformation(it, it.name )) }
                        add(HippoTabulator(consumerHeading, viewPlattformList))
                    }
                    // Logical addresses
                    cell {
                        val viewLogicalAddressList: MutableList<ViewInformation> = mutableListOf()
                        state.vLogicalAddresses.map {
                            viewLogicalAddressList.add(
                                ViewInformation(it, "<i>${it.description}</i><br>${it.name}"))
                        }
                        add(HippoTabulator(consumerHeading, viewLogicalAddressList))
                    }
                    // Service producers
                    cell {
                        val viewProducerLst: MutableList<ViewInformation> = mutableListOf()
                        state.vServiceProducers.map { viewProducerLst.add(ViewInformation(it, "<i>${it.description}</i><br>${it.hsaId}")) }
                        add(HippoTabulator(producerHeading, viewProducerLst))
                    }
                }.apply {
                    color = Color(Col.BLACK)
                }
            }.apply {
                color = Color(0x009090)

            }
        }
    }
}

private fun Container.informationText(state: HippoState) {
    if (state.downloadingBaseItems) {
        span(I18n.tr("Loading ..."))
    } else if (state.errorMessage != null) {
        div(state.errorMessage)
    } else {
        span(I18n.tr("Loading completed"))
    }
}

class HippoTabulator(
    columnHeader: String,
    itemList: List<ViewInformation>
): SimplePanel() {
    init {
        tabulator(
            itemList,
            options = TabulatorOptions(
                columns = listOf(
                    ColumnDefinition(
                        columnHeader, "showData",

                        formatterComponentFunction = { _, _, item ->
                            //console.log("In ColumnDef: ${item.name}")
                            Div(rich = true) { +item.showData }
                            //Div() { +item.hsaId }
                        },

                        headerFilter = Editor.INPUT
                    )
                ),
                pagination = PaginationMode.LOCAL,
                //height = "611px",
                height = "80vh",
                paginationSize = 100
            )
        )
        {
            //height = 430.px
            width = 100.perc
        }
    }
}

fun createViewData(state: HippoState) {

    // Extract plattform chains
    val plattformChains =
        state.integrationArrs.asSequence()
            .map { integration: Integration ->
                PlattformChain.calculateId(
                    integration.firstTpId,
                    integration.middleTpId,
                    integration.lastTpId
                )
            }
            .distinct()
            .map { PlattformChain.map.getValue(it) }
            .toList()

    // Extract logical addresses
    println("Will parse LA")
    val logicalAddresses =
        state.integrationArrs.asSequence()
            .map { integration: Integration -> integration.logicalAddressId } //iarr: Array<Int> -> iarr[4] }
            .distinct()
            .map { LogicalAddress.map[it] ?: LogicalAddress(-1, "", "") }
            .sortedWith(compareBy(LogicalAddress::description))
            .toList()

    // Domains must be added before the contracts
    println("Will parse domains")
    val serviceDomains =
        state.integrationArrs.asSequence()
            .map { integration: Integration -> integration.serviceDomainId }
            .distinct()
            .map {
                ServiceDomain.map[it] ?: ServiceDomain(id = -1, name = "")
            }
            .sortedWith(compareBy(ServiceDomain::name))
            .toList()

    // Contracts
    println("Will parse contracts")
    val serviceContracts =
        state.integrationArrs.asSequence()
            .map { integration: Integration -> integration.serviceContractId }
            .distinct()
            .map {
                ServiceContract.map[it]
                    ?: ServiceContract(-1, -1, "", "", -1)
            }
            .sortedWith(compareBy(ServiceContract::description))
            .toList()


    // Consumers
    println("Will parse consumers")
    val serviceConsumers =
        state.integrationArrs.asSequence()
            .map { integration: Integration -> integration.serviceConsumerId }
            .distinct()
            .map {
                ServiceComponent.map[it] ?: ServiceComponent(-1, "", "")
            }
            .sortedWith(compareBy(ServiceComponent::description))
            .toList()

    println("Will parse producers")
    val serviceProducers =
        state.integrationArrs.asSequence()
            .map { integration: Integration -> integration.serviceProducerId }
            .distinct()
            .map {
                ServiceComponent.map[it] ?: ServiceComponent(-1, "", "")
            }
            .sortedWith(compareBy(ServiceComponent::description))
            .toList()

    // Let us now populate the domainsAndContracts list (used in the hippo GUI)
    // Must be done via a temp list to stop the UI to update like crazy
    println("Will create the domainsAndContracts")
    val domainsAndContracts = mutableListOf<BaseItem>()
    for (domain in serviceDomains) {
        addUnique(domain, domainsAndContracts)

        // Need to get hold of the actual domain, not only the BaseItem version of it
        val domainId = domain.id
        val actualDomain = ServiceDomain.map[domainId]

        for (contract in serviceContracts) {
            if (contract in actualDomain!!.contracts) {
                addUnique(contract, domainsAndContracts)
            }
        }
    }

    store.dispatch(
        HippoAction.ViewUpdated(
            serviceConsumers,
            serviceProducers,
            serviceDomains,
            serviceContracts,
            domainsAndContracts,
            plattformChains,
            logicalAddresses
        )
    )
}

private fun addUnique(item: BaseItem, list: MutableList<BaseItem>) {
    if (list.contains(item)) return
    list.add(item)
}