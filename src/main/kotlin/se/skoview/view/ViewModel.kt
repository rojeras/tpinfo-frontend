package se.skoview.view

import pl.treksoft.kvision.form.time.DateTime
import pl.treksoft.kvision.i18n.tr
import se.skoview.data.*
import kotlin.js.Date

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
    // Extract plattform chains

    //val state = store.getState()
    val start = Date().getMilliseconds()
    val filteredIntegrations = filterViewData(state)
    val end = Date().getMilliseconds()
    println("Elapsed time: ${end - start} ms")

    //println(">>> Start createViewData()")
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
    //println("<<< End createViewData()")
    store.dispatch(HippoAction.ViewUpdated(integrationLists))
}

fun filterViewData(state: HippoState): List<Integration> {

    println(">>>> Start filterViewData")
    val integrationListsIn = state.integrationArrs

    val consumerFilter = state.consumerFilter
    val contractFilter = state.contractFilter
    val domainFilter = state.domainFilter
    val logicalAddressFilter = state.logicalAddressFilter
    val producerFilter = state.producerFilter
    val plattformChainFilter = state.plattformChainFilter

    val selectedConsumers = state.selectedConsumers
    val selectedContracts = state.selectedContracts
    val selectedDomains = state.selectedDomains
    val selectedPlattformChain = state.selectedPlattformChains
    val selectedLogicalAddresses = state.selectedLogicalAddresses
    val selectedProducers = state.selectedProducers

    val consumerFilterIds = filterItems(ServiceComponent.map as HashMap<Int, BaseItem>, consumerFilter)
    val contractFilterIds = filterItems(ServiceContract.map as HashMap<Int, BaseItem>, contractFilter)
    val domainFilterIds = filterItems(ServiceDomain.map as HashMap<Int, BaseItem>, domainFilter)
    val plattformChainFilterIds = filterItems(PlattformChain.map as HashMap<Int, BaseItem>, plattformChainFilter)
    //val laFilterIds = filterItems(LogicalAddress.map as HashMap<Int, BaseItem>, logicalAddressFilter)
    val producerFilterIds = filterItems(ServiceComponent.map as HashMap<Int, BaseItem>, producerFilter)

    // Loop through the list and remove all items which does not fulfill the filtering
    val resultList: MutableList<Integration> = mutableListOf()

    for (integration in integrationListsIn) {

        // Consumers
        if (
            selectedConsumers.isNotEmpty() &&
            (!selectedConsumers.contains(integration.serviceConsumerId))
        ) continue
        else if (
            consumerFilterIds.isNotEmpty() &&
            !consumerFilterIds.contains(integration.serviceConsumerId)
            /*
            consumerFilter.isNotEmpty() &&
            !ServiceComponent
                .map[integration.serviceConsumerId]!!
                .searchField
                .contains(
                    consumerFilter,
                    true
                )
             */
        ) continue

        // Contracts
        if (
            selectedContracts.isNotEmpty() &&
            (!selectedContracts.contains(integration.serviceContractId))
        ) continue
        else if (
            contractFilterIds.isNotEmpty() &&
            !contractFilterIds.contains(integration.serviceContractId)
            /*
            contractFilter.isNotEmpty() &&
            !ServiceContract
                .map[integration.serviceContractId]!!
                .searchField
                .contains(
                    contractFilter,
                    true
                )
             */
        ) continue

        // Domains
        if (
            selectedDomains.isNotEmpty() &&
            (!selectedDomains.contains(integration.serviceDomainId))
        ) continue
        else if (
            domainFilterIds.isNotEmpty() &&
            !domainFilterIds.contains(integration.serviceDomainId)
            /*
            domainFilter.isNotEmpty() &&
            !ServiceDomain
                .map[integration.serviceDomainId]!!
                .searchField
                .contains(
                    domainFilter,
                    true
                )
             */
        ) continue

        // Logical Addresses
        if (
            selectedLogicalAddresses.isNotEmpty() &&
            (!selectedLogicalAddresses.contains(integration.logicalAddressId))
        ) continue
        else if (
            //laFilterIds.isNotEmpty() &&
            //!laFilterIds.contains(integration.logicalAddressId)
            logicalAddressFilter.isNotEmpty() &&
                    !LogicalAddress
                        .map[integration.logicalAddressId]!!
                        .searchField
                        .contains(
                                logicalAddressFilter,
                                true
                        )
        ) continue

        // Producers
        if (
            selectedProducers.isNotEmpty() &&
            (!selectedProducers.contains(integration.serviceProducerId))
        ) continue
        else if (
            producerFilterIds.isNotEmpty() &&
            !producerFilterIds.contains(integration.serviceProducerId)
            /*
            producerFilter.isNotEmpty() &&
            !ServiceComponent
                .map[integration.serviceProducerId]!!
                .searchField
                .contains(
                    producerFilter,
                    true
                )
             */
        ) continue

        // Plattform Chains
        /*
        if (
            selectedPlattformChain.isNotEmpty() &&
            (!selectedPlattformChain.contains(integration.))
        ) continue
        */

        resultList.add(integration)
    }

    println("<<<< End filterViewData")

    return resultList
}

private fun addUnique(item: BaseItem, list: MutableList<BaseItem>) {
    if (list.contains(item)) return
    list.add(item)
}

// Make the free text search of the baseitems and collect hits as list of id's
private fun filterItems(map: HashMap<Int, BaseItem>, filter: String): List<Int> {
    val idList = mutableListOf<Int>()

    if (filter.isEmpty()) return idList

    for ((id, item) in map) {
        if (item.searchField.contains(filter, true)) idList.add(id)
    }

    return idList.distinct()
}