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

import se.skoview.app.store
import se.skoview.common.*

// todo: Speed up and simplify
data class IntegrationLists(
    val serviceConsumers: List<ServiceComponent>,
    val serviceProducers: List<ServiceComponent>,
    val serviceDomains: List<ServiceDomain>,
    val serviceContracts: List<ServiceContract>,
    val domainsAndContracts: List<BaseItem>,
    val plattformChains: List<PlattformChain>,
    val logicalAddresses: List<LogicalAddress>
)

fun createHippoViewData(state: HippoState) {
    //val filteredIntegrations = filterViewData(state)
    val filteredIntegrations = state.integrationArrs

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

private fun addUnique(item: BaseItem, list: MutableList<BaseItem>) {
    if (list.contains(item)) return
    list.add(item)
}
/*
fun filterViewData(state: HippoState): List<Integration> {
    // todo: Rewrite with sequence and filter to speed it up
    // One way to speed up might be to replace the Int in integrationArrs with an arrarr of acutal Baseitems
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
    //val result = integrationListsIn.asSequence() .filter { arr ->  }

    // Decide in which order the item types should be filtered
    // First do the ones that have been selected
    // Then the ones with a filter value
    // The longer filter value, the earlier it should be evaluated
    // First sort based on length of filter value, then just move the selected ones to the beginning of the prio list
    // Finally remove items which neither have filter nor are selected

    val selectedItemType = mapOf(
        ItemType.CONSUMER to selectedConsumers.isNotEmpty(),
        ItemType.PRODUCER to selectedProducers.isNotEmpty(),
        ItemType.CONTRACT to selectedContracts.isNotEmpty(),
        ItemType.DOMAIN to selectedDomains.isNotEmpty(),
        ItemType.LOGICAL_ADDRESS to selectedLogicalAddresses.isNotEmpty(),
        ItemType.PLATTFORM_CHAIN to selectedPlattformChain.isNotEmpty()
    )

    val filterLengthItemType = mapOf(
        ItemType.CONSUMER to consumerFilter.length,
        ItemType.PRODUCER to producerFilter.length,
        ItemType.CONTRACT to contractFilter.length,
        ItemType.DOMAIN to contractFilter.length,
        ItemType.LOGICAL_ADDRESS to logicalAddressFilter.length,
        ItemType.PLATTFORM_CHAIN to plattformChainFilter.length
    )

    val prioList: MutableList<ItemType> = mutableListOf(
        ItemType.CONSUMER,
        ItemType.PRODUCER,
        ItemType.CONTRACT,
        ItemType.DOMAIN,
        ItemType.LOGICAL_ADDRESS,
        ItemType.PLATTFORM_CHAIN
    )

    //println("filterLengthItemType: $filterLengthItemType")
    prioList.sortByDescending { filterLengthItemType[it] }

    for (type in prioList) {
        if (selectedItemType[type]!!) {
            prioList.remove(type)
            prioList.add(0, type)
        }
    }

    // todo: Remove the items which neither have a selection nor a filter from the priolist
    prioList.removeAll { filterLengthItemType[it] == 0 && !selectedItemType[it]!! }

    //println("Priolist: $prioList")

    // Loop through the list and remove all items which does not fulfill the filtering
    val resultList: MutableList<Integration> = mutableListOf()

    val consumerSearchCache = SearchCache(consumerFilter)
    val domainSearchCache = SearchCache(domainFilter)
    val contractSearchCache = SearchCache(contractFilter)
    val plattformChainSearchCache = SearchCache(plattformChainFilter)
    val logicalAddressSearchCache = SearchCache(logicalAddressFilter)
    val producerSearchCache = SearchCache(producerFilter)

    filtering@ for (integration in integrationListsIn) {

        // The order of evalutation is decided by the priolist
        for (eval in prioList) {
            when (eval) {
                ItemType.CONSUMER -> if (!isItemIncluded(
                        selectedConsumers,
                        consumerFilter,
                        ServiceComponent.map as HashMap<Int, BaseItem>,
                        integration.serviceConsumerId,
                        consumerSearchCache
                    )
                )
                    continue@filtering

                ItemType.CONTRACT -> if (!isItemIncluded(
                        selectedContracts,
                        contractFilter,
                        ServiceContract.map as HashMap<Int, BaseItem>,
                        integration.serviceContractId,
                        contractSearchCache
                    )
                )
                    continue@filtering

                ItemType.DOMAIN -> if (!isItemIncluded(
                        selectedDomains,
                        domainFilter,
                        ServiceDomain.map as HashMap<Int, BaseItem>,
                        integration.serviceDomainId,
                        domainSearchCache
                    )
                )
                    continue@filtering

                ItemType.LOGICAL_ADDRESS -> if (!isItemIncluded(
                        selectedLogicalAddresses,
                        logicalAddressFilter,
                        LogicalAddress.map as HashMap<Int, BaseItem>,
                        integration.logicalAddressId,
                        logicalAddressSearchCache
                    )
                )
                    continue@filtering

                ItemType.PRODUCER -> if (!isItemIncluded(
                        selectedProducers,
                        producerFilter,
                        ServiceComponent.map as HashMap<Int, BaseItem>,
                        integration.serviceProducerId,
                        producerSearchCache
                    )
                )
                    continue@filtering

                ItemType.PLATTFORM_CHAIN -> if (!isItemIncluded(
                        selectedPlattformChain,
                        plattformChainFilter,
                        PlattformChain.map as HashMap<Int, BaseItem>,
                        integration.plattformChainId,
                        plattformChainSearchCache
                    )
                )
                    continue@filtering
            }
        }
        resultList.add(integration)
    }

    println("<<<< End filterViewData")
    return resultList
}

 */


/*
data class SearchCache(val filter: String) {
    val idCache = mutableListOf<Int>()

    fun doContain(item: BaseItem): Boolean {
        val id = item.id
        if (idCache.binarySearch(id) >= 0) {
            return true
        }
        if (item.searchField.contains(filter, true)) {
            idCache.add(id)
            idCache.sort()
            return true
        }
        return false
    }
}
*/
/*
private fun isItemIncluded(
    selectedItems: List<Int>,
    filter: String,
    map: HashMap<Int, BaseItem>,
    itemId: Int,
    searchCache: SearchCache
): Boolean {
    if (
        selectedItems.isNotEmpty() &&
        (!selectedItems.contains(itemId))
    ) return false
    else if (
        filter.isNotEmpty() && !searchCache.doContain(map[itemId]!!)
    ) return false

    return true
}
 */

