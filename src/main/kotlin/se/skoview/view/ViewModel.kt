package se.skoview.view

import pl.treksoft.kvision.i18n.tr
import se.skoview.data.*

data class IntegrationLists(
    val serviceConsumers: List<ServiceComponent>,
    val serviceProducers: List<ServiceComponent>,
    val serviceDomains: List<ServiceDomain>,
    val serviceContracts: List<ServiceContract>,
    val domainsAndContracts: List<BaseItem>,
    val plattformChains: List<PlattformChain>,
    val logicalAddresses: List<LogicalAddress>
)

fun createViewData(state: HippoState) {
    println(">>> Start createViewData()")
    // Extract plattform chains

    //val state = store.getState()

    val filteredIntegrations = filterViewData(state)

    val plattformChains =
        filteredIntegrations.asSequence()
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
    val logicalAddresses =
        filteredIntegrations.asSequence()
            .map { integration: Integration -> integration.logicalAddressId } //iarr: Array<Int> -> iarr[4] }
            .distinct()
            .map { LogicalAddress.map[it] ?: LogicalAddress(-1, "", "") }
            .sortedWith(compareBy(LogicalAddress::description))
            .toList()

    // Domains must be added before the contracts
    val serviceDomains =
        filteredIntegrations.asSequence()
            .map { integration: Integration -> integration.serviceDomainId }
            .distinct()
            .map {
                ServiceDomain.map[it] ?: ServiceDomain(id = -1, name = "")
            }
            .sortedWith(compareBy(ServiceDomain::name))
            .toList()

    // Contracts

    val serviceContracts =
        filteredIntegrations.asSequence()
            .map { integration: Integration -> integration.serviceContractId }
            .distinct()
            .map {
                ServiceContract.map[it]
                    ?: ServiceContract(-1, -1, "", "", -1)
            }
            .sortedWith(compareBy(ServiceContract::description))
            .toList()


    // Consumers
    val serviceConsumers =
        filteredIntegrations.asSequence()
            .map { integration: Integration -> integration.serviceConsumerId }
            .distinct()
            .map {
                ServiceComponent.map[it] ?: ServiceComponent(-1, "", "")
            }
            .sortedWith(compareBy(ServiceComponent::description))
            .toList()

    val serviceProducers =
        filteredIntegrations.asSequence()
            .map { integration: Integration -> integration.serviceProducerId }
            .distinct()
            .map {
                ServiceComponent.map[it] ?: ServiceComponent(-1, "", "")
            }
            .sortedWith(compareBy(ServiceComponent::description))
            .toList()

    // Let us now populate the domainsAndContracts list (used in the hippo GUI)
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

    val integrationLists = IntegrationLists(
        serviceConsumers,
        serviceProducers,
        serviceDomains,
        serviceContracts,
        domainsAndContracts,
        plattformChains,
        logicalAddresses
    )
    println("<<< End createViewData()")
    store.dispatch(HippoAction.ViewUpdated(integrationLists))
}

fun filterViewData(state: HippoState): List<Integration> {

    println(">>>> Start filterViewData")
    val integrationListsIn = state.integrationArrs

    val consumerFilter = state.consumerFilter
    val contractFilter = ""
    val laFilter = ""
    val producerFilter = ""

    val selectedConsumers = state.selectedConsumers

    // Loop through the list and remove all items which does not fulfill the filtering
    val resultList: MutableList<Integration> = mutableListOf()

    for (integration in integrationListsIn) {

        if (
            selectedConsumers.isNotEmpty() &&
            (!selectedConsumers.contains(integration.serviceConsumerId))
        ) continue
        else if (
            consumerFilter.isNotEmpty() &&
            !ServiceComponent
                .map[integration.serviceConsumerId]!!
                .searchField
                .contains(
                    consumerFilter,
                    true
                )
        ) continue

        if (contractFilter.isNotEmpty() && !ServiceContract.map[integration.serviceContractId]!!.searchField.contains(
                contractFilter,
                true
            )
        ) continue
        if (laFilter.isNotEmpty() && !LogicalAddress.map[integration.logicalAddressId]!!.searchField.contains(
                laFilter,
                true
            )
        ) continue
        if (producerFilter.isNotEmpty() && !ServiceComponent.map[integration.serviceProducerId]!!.searchField.contains(
                producerFilter,
                true
            )
        ) continue

        resultList.add(integration)
    }

    println("<<<< End filterViewData")

    return resultList
}

private fun addUnique(item: BaseItem, list: MutableList<BaseItem>) {
    if (list.contains(item)) return
    list.add(item)
}