package se.skoview.app

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.Align
import pl.treksoft.kvision.html.div
import pl.treksoft.kvision.html.h2
import pl.treksoft.kvision.html.span
import pl.treksoft.kvision.i18n.I18n
import pl.treksoft.kvision.panel.FlexAlignItems
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.root
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.state.stateBinding
import pl.treksoft.kvision.utils.auto
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px
import se.skoview.data.*

object hippoPage : SimplePanel() {
    init {
        println("In hippoPage")
        vPanel(alignItems = FlexAlignItems.STRETCH) {
            width = 100.perc
            //searchField()
            span { +"Message: " }
            vPanel(alignItems = FlexAlignItems.STRETCH) {
                maxWidth = 1200.px
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
        }
        vPanel {
            div {
                h2("KHippo - integrationer via tjänsteplattform/ar för nationell e-hälsa")
                div("Integrationer för tjänsteplattformar vars tjänstadresseringskatalog (TAK) är tillgänglig i Ineras TAK-api visas.").apply {
                    width = 100.perc
                }
            }.apply {
                width = 100.perc
                background = Background(Col.CADETBLUE)
                align = Align.CENTER
                color = Color(Col.WHITE)
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