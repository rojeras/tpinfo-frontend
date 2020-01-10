package se.skoview.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import pl.treksoft.kvision.data.BaseDataComponent
import pl.treksoft.kvision.state.ObservableListWrapper
import pl.treksoft.kvision.state.observableListOf
import se.skoview.app.createViewData
import se.skoview.lib.getAsync
import se.skoview.lib.getAsyncTpDb
import kotlin.js.Date

enum class ItemType {
    CONSUMER,
    PRODUCER,
    LOGICAL_ADDRESS,
    DOMAIN,
    CONTRACT,
    DATE_EFFECTIVE,
    DATE_END,
    PLATTFORM,
    FIRST_PLATTFORM,
    MIDDLE_PLATTFORM,
    LAST_PLATTFORM,
    PLATTFORM_CHAIN
}

val TYPE_PARAM: Map<ItemType, String> = mapOf(
    ItemType.CONSUMER to "&consumerId=",
    ItemType.PRODUCER to "&producerId=",
    ItemType.LOGICAL_ADDRESS to "&logicalAddressId=",
    ItemType.CONTRACT to "&contractId=",
    ItemType.DOMAIN to "&domainId=",
    ItemType.FIRST_PLATTFORM to "&firstPlattformId=",
    ItemType.MIDDLE_PLATTFORM to "&middlePlattformId=",
    ItemType.LAST_PLATTFORM to "&lastPlattformId=",
    ItemType.DATE_END to "&dateEnd=",
    ItemType.DATE_EFFECTIVE to "&dateEffective="
)

/**
 * Defines all integrations for the current filter. The filter is provided as a parameter to the factory function mkIntegrationInfo(filter)
 * An IntegrationsInfo object should not change after it has been created.
 * Each object is defined by the filter bookmark, and a map companion contains all objects
 */

@Serializable
data class IntegrationInfo(
    val integrations: List<List<Int?>>,
    val maxCounters: MaxCounter,
    val updateDates: List<String>
)

@Serializable
data class Integration(
    val firstTpId: Int,
    val middleTpId: Int?,
    val lastTpId: Int,
    val logicalAddressId: Int,
    val serviceContractId: Int,
    val serviceDomainId: Int,
    val serviceConsumerId: Int,
    val serviceProducerId: Int
)

@Serializable
data class MaxCounter(
    val consumers: Int,
    val contracts: Int,
    val domains: Int,
    val plattformChains: Int,
    val logicalAddress: Int,
    val producers: Int
)

fun loadIntegrations(state: HippoState) {
    store.dispatch(HippoAction.StartDownloadIntegrations)
    val urlParameters = state.getParams()
    val parameters = "integrations$urlParameters"

    getAsyncTpDb(parameters) { response ->
        println("Size of response is: ${response.length}")
        val json = Json(JsonConfiguration.Stable)
        val integrationInfo: IntegrationInfo = json.parse(IntegrationInfo.serializer(), response)
        console.log(integrationInfo)
        val integrationArrs: MutableList<Integration> = mutableListOf()
        for (arr: List<Int?> in integrationInfo.integrations) {
            val one: Int = arr[1] ?: -1
            val two: Int? = arr[2]
            val three: Int = arr[3] ?: -1
            val four: Int = arr[4] ?: -1
            val five: Int = arr[5] ?: -1
            val six: Int = arr[6] ?: -1
            val seven: Int = arr[7] ?: -1
            val eight: Int = arr[8] ?: -1

            integrationArrs.add(Integration(one, two, three, four, five, six, seven, eight))
        }
        store.dispatch(HippoAction.DoneDownloadIntegrations(integrationArrs, integrationInfo.maxCounters, integrationInfo.updateDates))
        createViewData(store.getState())
    }
}

/*
// todo: Should make the objects of this class immutable, including the filter
// And I do not need observerableLists here, these objects ought to be immutable
class TpdbIntegrations() : BaseDataComponent() {

    val plattformChains = observableListOf<BaseItem>()
    val logicalAddresses = observableListOf<BaseItem>()
    val serviceContracts = observableListOf<BaseItem>()
    val serviceDomains = observableListOf<BaseItem>()
    val domainsAndContracts = observableListOf<BaseItem>()
    val serviceConsumers = observableListOf<BaseItem>()
    val serviceProducers = observableListOf<BaseItem>()
    val updateDates = observableListOf<Date>()
    val maxCounterConsumer = observableListOf<Int>()
    val maxCounterContract = observableListOf<Int>()
    val maxCounterPlattformChain = observableListOf<Int>()
    val maxCounterLogicalAddress = observableListOf<Int>()
    val maxCounterProducer = observableListOf<Int>()

    val dateOptionsList: ObservableListWrapper<Pair<String, String>> = observableListOf<Pair<String, String>>()

    //lateinit var bookmark: String
    lateinit var filter: Filter

    // todo: Is this really the right date to use to initialize the range
    //var maxCounters = mutableMapOf<ItemType, Int>()

    private fun loadIntegrations(urlParameters: String, postLoadCallback: (iI: TpdbIntegrations) -> Unit) {
        val parameters = "integrations$urlParameters"

        // Following data class only used for JSON parsing
        open class MaxCounterJsonParse(
            val consumers: Int = -1,
            val contracts: Int = -1,
            val domains: Int = -1,
            val plattformChains: Int = -1,
            val logicalAddress: Int = -1,
            val producers: Int = -1
        )

        data class IntegrationJsonParse(
            val integrations: Array<Array<Int>>,
            val maxCounters: MaxCounterJsonParse,
            val updateDates: Array<String>
        )

        getAsyncTpDb(parameters) { response ->
            println("Result is comming back from server")
            val integrationArrArr: IntegrationJsonParse = JSON.parse(response)
            println("Parsing done")

            val tempUpdateDates = mutableListOf<Date>()
            val tempDateOptionList = mutableListOf<Pair<String, String>>()
            for (dateString in integrationArrArr.updateDates) {
                tempUpdateDates.add(Date(dateString))
                tempDateOptionList.add(Pair(dateString, dateString))
            }
            this.updateDates.clear()
            this.updateDates.addAll(tempUpdateDates)
            this.dateOptionsList.clear()
            this.dateOptionsList.addAll(tempDateOptionList)

            // todo: Handle the nullable stuff so I do not need the clumpsy elvis constructs
            // First we collect the unique lists of items per type
            println("Will parse plattformchains")
            //this.plattformChains.clear()
            this.plattformChains.addAll(
                integrationArrArr.integrations.asSequence()
                    .map { iarr: Array<Int> -> PlattformChain.calculateId(iarr[1], iarr[2], iarr[3]) }
                    .distinct()
                    //.map { BASE_ITEMS.plattFormChains.getValue(it) }
                    .map { PlattformChain.map.getValue(it) }
                    .toList()
            )
            println("Will parse LA")
            this.logicalAddresses.clear()
            this.logicalAddresses.addAll(
                integrationArrArr.integrations.asSequence()
                    .map { iarr: Array<Int> -> iarr[4] }
                    .distinct()
                    .map { LogicalAddress.map[it] ?: LogicalAddress(-1, "", "") }
                    .sortedWith(compareBy(LogicalAddress::description))
                    .toList()
            )

            // Domains must be added before the contracts
            println("Will parse domains")
            this.serviceDomains.clear()
            this.serviceDomains.addAll(
                integrationArrArr.integrations.asSequence()
                    .map { iarr: Array<Int> -> iarr[6] }
                    .distinct()
                    .map {
                        ServiceDomain.map[it] ?: ServiceDomain(id = -1, name = "")
                    }
                    .sortedWith(compareBy(ServiceDomain::name))
                    .toList()
            )

            println("Will parse contracts")
            this.serviceContracts.clear()
            this.serviceContracts.addAll(
                integrationArrArr.integrations.asSequence()
                    .map { iarr: Array<Int> -> iarr[5] }
                    .distinct()
                    .map {
                        ServiceContract.map[it]
                            ?: ServiceContract(-1, -1, "", "", -1)
                    }
                    .sortedWith(compareBy(ServiceContract::description))
                    .toList()
            )

            println("Will parse consumers")
            this.serviceConsumers.clear()
            this.serviceConsumers.addAll(
                integrationArrArr.integrations.asSequence()
                    .map { iarr: Array<Int> -> iarr[7] }
                    .distinct()
                    .map {
                        ServiceComponent.map[it] ?: ServiceComponent(-1, "", "")
                    }
                    .sortedWith(compareBy(ServiceComponent::description))
                    .toList()
            )

            println("Will parse producers")
            this.serviceProducers.clear()
            this.serviceProducers.addAll(
                integrationArrArr.integrations.asSequence()
                    .map { iarr: Array<Int> -> iarr[8] }
                    .distinct()
                    .map {
                        ServiceComponent.map[it] ?: ServiceComponent(-1, "", "")
                    }
                    .sortedWith(compareBy(ServiceComponent::description))
                    .toList()
            )

            // Let us now populate the domainsAndContracts list (used in the hippo GUI)
            // Must be done via a temp list to stop the UI to update like crazy
            println("Will create the donainsAndContracts")
            val tempList = mutableListOf<BaseItem>()
            for (domain in this.serviceDomains) {
                this.addUnique(domain, tempList)

                // Need to get hold of the actual domain, not only the BaseItem version of it
                val domainId = domain.id
                val actualDomain = ServiceDomain.map[domainId]

                for (contract in this.serviceContracts) {
                    if (contract in actualDomain!!.contracts) {
                        this.addUnique(contract, tempList)
                    }
                }
            }
            this.domainsAndContracts.clear()
            this.domainsAndContracts.addAll(tempList)

            this.maxCounterConsumer.clear()
            this.maxCounterContract.clear()
            this.maxCounterPlattformChain.clear()
            this.maxCounterLogicalAddress.clear()
            this.maxCounterProducer.clear()
            this.maxCounterConsumer.add(integrationArrArr.maxCounters.consumers)
            this.maxCounterContract.add(integrationArrArr.maxCounters.contracts)
            this.maxCounterPlattformChain.add(integrationArrArr.maxCounters.plattformChains)
            this.maxCounterLogicalAddress.add(integrationArrArr.maxCounters.logicalAddress)
            this.maxCounterProducer.add(integrationArrArr.maxCounters.producers)

            postLoadCallback(this)
        }
    }

    companion object {
        private val map = hashMapOf<String, TpdbIntegrations>()

        fun mkTpdbInfo(filter: Filter, postLoadCallback: (iI: TpdbIntegrations) -> Unit): TpdbIntegrations {
            // Place to add cache support

            val filterBookmark = filter.bookmark()
            val cachedIntegrationInfo = map[filterBookmark]

            return if (cachedIntegrationInfo != null) {
                println("Found a cached integrationInfo with bookmark: $filterBookmark")
                postLoadCallback(cachedIntegrationInfo)
                cachedIntegrationInfo
            } else {
                println("Cound NOT find a cached integrationInfo for bookmark: $filterBookmark")
                val integrationInfo = TpdbIntegrations()
                integrationInfo.loadIntegrations(filter.getParams(), postLoadCallback)
                integrationInfo.filter = filter.copy() // Must make sure the filter is not updated
                map[filter.bookmark()] = integrationInfo

                integrationInfo
            }
        }
    }

    private fun addUnique(item: BaseItem, list: MutableList<BaseItem>) {
        if (list.contains(item)) return
        list.add(item)
    }
}

*/
