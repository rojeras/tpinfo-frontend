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
package se.skoview.data

import se.skoview.app.store
import se.skoview.lib.getAsyncTpDb
import se.skoview.view.loadHistory

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
    //var statisticsCache = StatisticsCache(parameters, mapOf(), mapOf(), mapOf(), mapOf(), mapOf())

    // Check if the statistics info is available in the cache
    if (StatisticsCache.map.containsKey(parameters)) {
        println("Statistics found in cache")
        val statisticsCache = StatisticsCache.map[parameters]!!

        store.dispatch { _, _ ->
            store.dispatch(
                HippoAction.DoneDownloadStatistics(
                    statisticsCache.callsConsumer,
                    statisticsCache.callsProducer,
                    statisticsCache.callsLogicalAddress,
                    statisticsCache.callsDomain,
                    statisticsCache.callsContract
                )
            )
            //SInfo.createStatViewData(getState())
        }
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

            val statisticsCache = StatisticsCache(
                parameters,
                ackConsumerMap,
                ackProducerMap,
                ackLogicalAddressMap,
                ackDomainMap,
                ackContractMap
            )

            store.dispatch { _, _ ->
                store.dispatch(
                    HippoAction.DoneDownloadStatistics(
                        statisticsCache.callsConsumer,
                        statisticsCache.callsProducer,
                        statisticsCache.callsLogicalAddress,
                        statisticsCache.callsDomain,
                        statisticsCache.callsContract
                    )
                )
                //SInfo.createStatViewData(getState())
            }
        }
    }
    loadHistory(state)

}

private fun updateCallsArr(callsArr: MutableMap<Int, Int>, itemId: Int, calls: Int) {
    if (callsArr[itemId] == null) callsArr[itemId] = 0
    callsArr[itemId] = callsArr[itemId]!!.plus(calls)

}
