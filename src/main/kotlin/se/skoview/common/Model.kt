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
package se.skoview.common

import se.skoview.hippo.BookmarkInformation
import se.skoview.hippo.parseBookmark
import se.skoview.stat.AdvancedViewPreSelect
import se.skoview.stat.SimpleViewPreSelect
import se.skoview.stat.StatisticsBlob
import se.skoview.stat.simpleViewPreSelectDefault
import kotlin.reflect.KClass

enum class AsyncActionStatus {
    NOT_INITIALIZED,
    INITIALIZED,
    COMPLETED,
    ERROR
}

enum class DateType {
    EFFECTIVE,
    END,
    EFFECTIVE_AND_END
}

enum class HippoApplication {
    HIPPO,
    STATISTIK
}

enum class ViewMode {
    SIMPLE,
    ADVANCED
}

data class HippoState(
    // Status information
    val currentAction: KClass<out HippoAction> = HippoAction.ApplicationStarted::class,
    val view: View = View.HOME,
    val applicationStarted: HippoApplication? = null,
    val downloadBaseItemStatus: AsyncActionStatus = AsyncActionStatus.NOT_INITIALIZED,
    val downloadIntegrationStatus: AsyncActionStatus = AsyncActionStatus.NOT_INITIALIZED,
    val errorMessage: String? = null,

    // Base Items
    // todo: Why are the base items stored via the state? Ought to be enough to register when they are loaded.
    val integrationDates: List<String> = listOf(),
    val statisticsDates: List<String> = listOf(),
    val serviceComponents: Map<Int, ServiceComponent> = mapOf(),
    val logicalAddresses: Map<Int, LogicalAddress> = mapOf(),
    val serviceContracts: Map<Int, ServiceContract> = mapOf(),
    val serviceDomains: Map<Int, ServiceDomain> = mapOf(),
    val plattforms: Map<Int, Plattform> = mapOf(),
    val plattformChains: Map<Int, PlattformChain> = mapOf(),
    val statisticsPlattforms: Map<Int, StatisticsPlattform> = mapOf(),

    // Filter parameters

    val dateEffective: String? = null,
    val dateEnd: String? = null,

    val selectedConsumers: List<Int> = listOf(),
    val selectedProducers: List<Int> = listOf(),
    val selectedLogicalAddresses: List<Int> = listOf(),
    val selectedContracts: List<Int> = listOf(),
    val selectedDomains: List<Int> = listOf(),
    val selectedPlattformChains: List<Int> = listOf(),

    // Integrations data
    val selectedPlattformName: String = "",
    val integrationArrs: List<Integration> = listOf(),
    val maxCounters: MaxCounter = MaxCounter(0, 0, 0, 0, 0, 0),
    val updateDates: List<String> = listOf(),

    // View data
    val vServiceConsumers: List<ServiceComponent> = listOf(),
    val vServiceProducers: List<ServiceComponent> = listOf(),
    val vServiceDomains: List<ServiceDomain> = listOf(),
    val vServiceContracts: List<ServiceContract> = listOf(),
    val vDomainsAndContracts: List<BaseItem> = listOf(),
    val vPlattformChains: List<PlattformChain> = listOf(),
    val vLogicalAddresses: List<LogicalAddress> = listOf(),

    // Max number of items to display
    val vServiceConsumersMax: Int = 100,
    val vServiceProducersMax: Int = 100,
    val vLogicalAddressesMax: Int = 100,
    val vServiceContractsMax: Int = 500,

    // Statistics information
    val statBlob: StatisticsBlob = StatisticsBlob(arrayOf(arrayOf())),

    // History information
    val historyMap: Map<String, Int> = mapOf(),
    val showTimeGraph: Boolean = false,

    // View controllers
    val showTechnicalTerms: Boolean = false,
    val viewMode: ViewMode = ViewMode.SIMPLE,
    val simpleViewPreSelect: SimpleViewPreSelect = simpleViewPreSelectDefault,
    val advancedViewPreSelect: AdvancedViewPreSelect? = null
)

fun initializeHippoState(): HippoState {
    val bookmarkInformation: BookmarkInformation = parseBookmark()

    val state = HippoState()
    return state.copy(
        dateEffective = bookmarkInformation.dateEffective,
        dateEnd = bookmarkInformation.dateEnd,
        selectedConsumers = bookmarkInformation.selectedConsumers,
        selectedProducers = bookmarkInformation.selectedProducers,
        selectedLogicalAddresses = bookmarkInformation.selectedLogicalAddresses,
        selectedContracts = bookmarkInformation.selectedContracts,
        selectedDomains = bookmarkInformation.selectedDomains,
        selectedPlattformChains = bookmarkInformation.selectedPlattformChains
    )
}

fun HippoState.isItemSelected(itemType: ItemType, id: Int): Boolean {
    return when (itemType) {
        ItemType.CONSUMER -> this.selectedConsumers.contains(id)
        ItemType.DOMAIN -> this.selectedDomains.contains(id)
        ItemType.CONTRACT -> this.selectedContracts.contains(id)
        ItemType.PLATTFORM_CHAIN -> this.selectedPlattformChains.contains(id)
        ItemType.LOGICAL_ADDRESS -> this.selectedLogicalAddresses.contains(id)
        ItemType.PRODUCER -> this.selectedProducers.contains(id)
    }
}

fun HippoState.isPlattformSelected(id: Int): Boolean {
    val pChainId = PlattformChain.calculateId(first = id, middle = null, last = id)

    return this.selectedPlattformChains.contains(pChainId)
}

// The extension function create the part of the URL to fetch integrations
fun HippoState.getParams(): String {

    // var params = "?dummy&contractId=379"
    var params = "?dummy"

    params += "&dateEffective=" + this.dateEffective
    params += "&dateEnd=" + this.dateEnd

    params += if (this.selectedConsumers.isNotEmpty()) this.selectedConsumers.joinToString(
        prefix = "&consumerId=",
        separator = ","
    ) else ""
    params += if (this.selectedDomains.isNotEmpty()) this.selectedDomains.joinToString(
        prefix = "&domainId=",
        separator = ","
    ) else ""
    params += if (this.selectedContracts.isNotEmpty()) this.selectedContracts.joinToString(
        prefix = "&contractId=",
        separator = ","
    ) else ""
    params += if (this.selectedLogicalAddresses.isNotEmpty()) this.selectedLogicalAddresses.joinToString(
        prefix = "&logicalAddressId=",
        separator = ","
    ) else ""
    params += if (this.selectedProducers.isNotEmpty()) this.selectedProducers.joinToString(
        prefix = "&producerId=",
        separator = ","
    ) else ""

    // Separate plattforms now stored in filter, not the chain
    for (pcId in this.selectedPlattformChains) {
        val firstId = PlattformChain.map[pcId]?.first
        val lastId = PlattformChain.map[pcId]?.last
        params += "&firstPlattformId=$firstId"
        params += "&lastPlattformId=$lastId"
    }

    return params
}
