package se.skoview.view

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

fun createViewData(state: HippoState): IntegrationLists {
    println(":::::::::::: Start createViewData() ::::::::::::::::")
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
    val logicalAddresses =
        state.integrationArrs.asSequence()
            .map { integration: Integration -> integration.logicalAddressId } //iarr: Array<Int> -> iarr[4] }
            .distinct()
            .map { LogicalAddress.map[it] ?: LogicalAddress(-1, "", "") }
            .sortedWith(compareBy(LogicalAddress::description))
            .toList()

    // Domains must be added before the contracts
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
    val serviceConsumers =
        state.integrationArrs.asSequence()
            .map { integration: Integration -> integration.serviceConsumerId }
            .distinct()
            .map {
                ServiceComponent.map[it] ?: ServiceComponent(-1, "", "")
            }
            .sortedWith(compareBy(ServiceComponent::description))
            .toList()

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
    println(":::::::::::: End createViewData() ::::::::::::::::")
    return integrationLists
/*
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
 */
}

private fun addUnique(item: BaseItem, list: MutableList<BaseItem>) {
    if (list.contains(item)) return
    list.add(item)
}