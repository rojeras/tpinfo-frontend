package se.skoview.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import se.skoview.app.store
import se.skoview.lib.getAsyncTpDb
import se.skoview.view.createViewData
import se.skoview.view.getBookmark
import se.skoview.view.setUrlFilter
import kotlin.browser.window

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
) {
    val plattformChainId = PlattformChain.calculateId(firstTpId, middleTpId, lastTpId)
}

@Serializable
data class MaxCounter(
    val consumers: Int,
    val contracts: Int,
    val domains: Int,
    val plattformChains: Int,
    val logicalAddress: Int,
    val producers: Int
)

data class IntegrationCache(
    val key: String,
    val integrationArr: List<Integration>,
    val maxCounters: MaxCounter,
    val updateDates: List<String>
) {
    init {
        map[key] = this
    }

    companion object {
        val map: HashMap<String, IntegrationCache> = hashMapOf<String, IntegrationCache>()
    }
}

fun loadIntegrations(state: HippoState) {
    store.dispatch(HippoAction.StartDownloadIntegrations)
    //setUrlFilter(state)
    val urlParameters = state.getParams()
    val parameters = "integrations$urlParameters"

    // Check if the integration info is available in the cache
    if (IntegrationCache.map.containsKey(parameters)) {
        println("Integrations found in cache")
        val integrationsCache = IntegrationCache.map[parameters]
        // todo: Make sure to remove the !! below
        store.dispatch(
            HippoAction.DoneDownloadIntegrations(
                integrationsCache!!.integrationArr,
                integrationsCache.maxCounters,
                integrationsCache.updateDates
            )
        )
    } else {
        println(">>> Integrations NOT found in cache - will download")
        console.log(parameters)
        getAsyncTpDb(parameters) { response ->
            println(">>> Size of fetched integrations is: ${response.length}")
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
                IntegrationCache(parameters, integrationArrs, integrationInfo.maxCounters, integrationInfo.updateDates)
            }
            println("Number of integrations: ${integrationArrs.size}")
            store.dispatch { action, getState ->
                store.dispatch(
                    HippoAction.DoneDownloadIntegrations(
                        integrationArrs,
                        integrationInfo.maxCounters,
                        integrationInfo.updateDates
                    )
                )
                createViewData(getState())
            }
        }
    }
}


