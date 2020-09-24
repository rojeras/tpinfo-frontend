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

import se.skoview.hippo.parseBookmark
import se.skoview.stat.StatPreSelect
import se.skoview.stat.StatisticsBlob
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

//@Serializable
data class HippoState(
    // Status information
    val currentAction: KClass<out HippoAction>,
    val applicationStarted: HippoApplication?,
    val downloadBaseItemStatus: AsyncActionStatus,
    val downloadIntegrationStatus: AsyncActionStatus,
    val errorMessage: String?,

    // Base Items
    val integrationDates: List<String>,
    val statisticsDates: List<String>,
    val serviceComponents: Map<Int, ServiceComponent>,
    val logicalAddresses: Map<Int, LogicalAddress>,
    val serviceContracts: Map<Int, ServiceContract>,
    val serviceDomains: Map<Int, ServiceDomain>,
    val plattforms: Map<Int, Plattform>,
    val plattformChains: Map<Int, PlattformChain>,
    val statisticsPlattforms: Map<Int, StatisticsPlattform>,

    // Filter parameters
    val dateEffective: String,
    val dateEnd: String,

    val selectedConsumers: List<Int>,
    val selectedProducers: List<Int>,
    val selectedLogicalAddresses: List<Int>,
    val selectedContracts: List<Int>,
    val selectedDomains: List<Int>,
    val selectedPlattformChains: List<Int>,
    val selectedPlattformName: String,

    // Integrations data
    val integrationArrs: List<Integration>,
    val maxCounters: MaxCounter,
    val updateDates: List<String>,

    // View data
    val vServiceConsumers: List<ServiceComponent>,
    val vServiceProducers: List<ServiceComponent>,
    val vServiceDomains: List<ServiceDomain>,
    val vServiceContracts: List<ServiceContract>,
    val vDomainsAndContracts: List<BaseItem>,
    val vPlattformChains: List<PlattformChain>,
    val vLogicalAddresses: List<LogicalAddress>,

    // Max number of items to display
    val vServiceConsumersMax: Int,
    val vServiceProducersMax: Int,
    val vLogicalAddressesMax: Int,
    val vServiceContractsMax: Int,

    // Statistics information
    val statBlob: StatisticsBlob,

    // History information
    val historyMap: Map<String, Int>,
    val showTimeGraph: Boolean,

    // View controllers
    val showTechnicalTerms: Boolean,
    val statAdvancedMode: Boolean,
    val statPreSelectLabel: String,
    val statPreSelect: StatPreSelect?,
    val consumerLabel: String,
    val contractLabel: String,
    val producerLabel: String,
    val laLabel: String

)

// This function creates the initial state based on an option filter parameter in the URL
fun initialHippoState(): HippoState {

    val bookmarkInformation = parseBookmark()

    return HippoState(
        HippoAction.ApplicationStarted::class,
        null,
        AsyncActionStatus.NOT_INITIALIZED,
        AsyncActionStatus.NOT_INITIALIZED,
        null,
        listOf(),
        listOf(),
        mapOf(),
        mapOf(),
        mapOf(),
        mapOf(),
        mapOf(),
        mapOf(),
        mapOf(),
        bookmarkInformation.dateEffective, // todo: Verify if this is a good default - really want empty value
        bookmarkInformation.dateEnd,
        //bookmarkInformation.selectedConsumers,
        listOf(304), // TEMP
        bookmarkInformation.selectedProducers,
        bookmarkInformation.selectedLogicalAddresses,
        bookmarkInformation.selectedContracts,
        bookmarkInformation.selectedDomains,
        bookmarkInformation.selectedPlattformChains,
        "",
        listOf(),
        MaxCounter(0, 0, 0, 0, 0, 0),
        listOf(),
        listOf(),
        listOf(),
        listOf(),
        listOf(),
        listOf(),
        listOf(),
        listOf(),
        100,
        100,
        100,
        500,
        StatisticsBlob(arrayOf(arrayOf())),
        mapOf(),
        false,
        false,
        true, // TEMP
        "Allt",
        null,
        "",
        "",
        "",
        ""
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

    //var params = "?dummy&contractId=379"
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



