package se.skoview.data

import kotlinx.serialization.Serializable
import se.skoview.app.store
import se.skoview.lib.getAsyncTpDb
import se.skoview.view.createViewData

data class StatisticsCache(
    val key: String,
    val callsConsumer: Map<Int, Int>,
    val callsProducer: Map<Int, Int>,
    val callsLogicalAddress: Map<Int, Int>,
    val callsDomain: Map<Int, Int>,
    val callsContract: Map<Int, Int>
) {
    init {
        map[key] = this
    }

    companion object {
        val map: HashMap<String, StatisticsCache> = hashMapOf<String, StatisticsCache>()
    }
}

fun loadStatistics(state: HippoState) {
    val urlParameters = state.getParams()
    val parameters = "statistics$urlParameters"

    // todo: Clean away this fakes instansiation. Needed to be able to use the var in the final dispatch.
    var statisticsCache = StatisticsCache("dummy", mapOf(), mapOf(), mapOf(), mapOf(), mapOf())

    // Check if the statistics info is available in the cache
    if (StatisticsCache.map.containsKey(parameters)) {
        println("Statistics found in cache")
        statisticsCache = StatisticsCache.map[parameters]!!
    } else {
        println(">>> Statistics data NOT found in cache - will download")
        console.log(parameters)
        getAsyncTpDb(parameters) { response ->
            println(">>> Size of fetched statistics data is: ${response.length}")

            val statisticsArrArr: Array<Array<Int>> = JSON.parse(response)

            val ackConsumerMap = mutableMapOf<Int, Int>()
            val ackProducerMap = mutableMapOf<Int, Int>()
            val ackLogicalAddressMap = mutableMapOf<Int, Int>()
            val ackContractMap = mutableMapOf<Int, Int>()
            val ackDomainMap = mutableMapOf<Int, Int>()
            //val ackPlattformMap = mutableMapOf<Int, Int>()

            for (arr in statisticsArrArr) {
                //updateCallsArr(ackPlattformMap, arr[0], arr[6])
                updateCallsArr(ackLogicalAddressMap, arr[1], arr[6])
                updateCallsArr(ackContractMap, arr[2], arr[6])
                updateCallsArr(ackDomainMap, arr[3], arr[6])
                updateCallsArr(ackConsumerMap, arr[4], arr[6])
                updateCallsArr(ackProducerMap, arr[5], arr[6])
            }
            println("Number of statistics records: ${statisticsArrArr.size}")

            statisticsCache = StatisticsCache(
                parameters,
                ackConsumerMap,
                ackProducerMap,
                ackLogicalAddressMap,
                ackDomainMap,
                ackContractMap
            )
        }
    }
    store.dispatch { _, getState ->
        store.dispatch(
            HippoAction.DoneDownloadStatistics(
                statisticsCache.callsConsumer,
                statisticsCache.callsProducer,
                statisticsCache.callsLogicalAddress,
                statisticsCache.callsDomain,
                statisticsCache.callsContract
            )
        )
        createViewData(getState())
    }
}

private fun updateCallsArr(callsArr: MutableMap<Int, Int>, itemId: Int, calls: Int) {
    if (callsArr[itemId] == null) callsArr[itemId] = 0
    callsArr[itemId] = callsArr[itemId]!!.plus(calls)

}
