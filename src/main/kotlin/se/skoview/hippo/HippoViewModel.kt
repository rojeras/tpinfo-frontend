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

import se.skoview.common.* // ktlint-disable no-wildcard-imports

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

fun createHippoViewData(state: HippoState): IntegrationLists {
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
            .map { PlattformChain.mapp.getValue(it) }
            .toList()

    // Extract logical addresses
    val logicalAddresses =
        filteredIntegrations.asSequence()
            .map { integration: Integration -> integration.logicalAddressId }
            .distinct()
            .map { LogicalAddress.mapp[it] ?: LogicalAddress(-1, "", "", "") }
            .sortedWith(compareBy(LogicalAddress::description))
            .toList()

    // Domains must be added before the contracts
    val serviceDomains =
        filteredIntegrations.asSequence()
            .map { integration: Integration -> integration.serviceDomainId }
            .distinct()
            .map {
                ServiceDomain.mapp[it] ?: ServiceDomain(id = -1, domainName = "")
            }
            .sortedWith(compareBy(ServiceDomain::name))
            .toList()

    // Contracts

    val serviceContracts =
        filteredIntegrations.asSequence()
            .map { integration: Integration -> integration.serviceContractId }
            .distinct()
            .map {
                ServiceContract.mapp[it]
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
                ServiceComponent.mapp[it] ?: ServiceComponent(-1, "", "")
            }
            .sortedWith(compareBy(ServiceComponent::description))
            .toList()

    val serviceProducers =
        filteredIntegrations.asSequence()
            .map { integration: Integration -> integration.serviceProducerId }
            .distinct()
            .map {
                ServiceComponent.mapp[it] ?: ServiceComponent(-1, "", "")
            }
            .sortedWith(compareBy(ServiceComponent::description))
            .toList()

    // Let us now populate the domainsAndContracts list (used in the hippo GUI)
    val domainsAndContracts = mutableListOf<BaseItem>()
    for (domain in serviceDomains) {
        addUnique(domain, domainsAndContracts)

        // Need to get hold of the actual domain, not only the BaseItem version of it
        val domainId = domain.id
        val actualDomain = ServiceDomain.mapp[domainId]

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
    return integrationLists
}

private fun addUnique(item: BaseItem, list: MutableList<BaseItem>) {
    if (list.contains(item)) return
    list.add(item)
}
