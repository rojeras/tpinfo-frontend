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

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import pl.treksoft.kvision.rest.HttpMethod
import pl.treksoft.kvision.rest.RestClient
import se.skoview.app.store
import se.skoview.hippo.createHippoViewData

enum class ItemType {
    CONSUMER,
    PRODUCER,
    LOGICAL_ADDRESS,
    DOMAIN,
    CONTRACT,
    PLATTFORM_CHAIN
}

@Serializable
data class IntegrationInfo(
    val integrations: Array<Array<Int?>>,
    val maxCounters: MaxCounter,
    val updateDates: Array<String>
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

data class IntegrationCache(
    val key: String,
    val integrationArr: List<Integration>,
    val maxCounters: MaxCounter,
    val updateDates: Array<String>
) {
    init {
        map[key] = this
    }

    companion object {
        val map: HashMap<String, IntegrationCache> = hashMapOf<String, IntegrationCache>()
    }
}

fun loadIntegrations(state: HippoState) {
    val urlParameters = state.getParams()
    val parameters = "integrations$urlParameters"

    if (IntegrationCache.map.containsKey(parameters)) {
        println(">>> Integrations found in cache")
        val integrationsCache = IntegrationCache.map[parameters]
        // todo: Make sure to remove the !! below
        store.dispatch(
            HippoAction.DoneDownloadIntegrations(
                integrationsCache!!.integrationArr,
                integrationsCache.maxCounters,
                integrationsCache.updateDates
            )
        )
        createHippoViewData(store.getState())
    } else {
        println(">>> Integrations NOT found in cache - will download")

        val restClient = RestClient()

        val url = "${tpdbBaseUrl()}$parameters"
        println(url)

        val job = GlobalScope.launch {
            val integrationInfoPromise =
                restClient.remoteCall(
                    url = url,
                    method = HttpMethod.GET,
                    deserializer = IntegrationInfo.serializer(),
                    contentType = ""
                )
            val integrationInfo = integrationInfoPromise.await()

            val integrationArrs: MutableList<Integration> = mutableListOf()
            for (arr: Array<Int?> in integrationInfo.integrations) {
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

            IntegrationCache(parameters, integrationArrs, integrationInfo.maxCounters, integrationInfo.updateDates)

            println("Number of integrations: ${integrationArrs.size}")
            store.dispatch(
                HippoAction.DoneDownloadIntegrations(
                    integrationArrs,
                    integrationInfo.maxCounters,
                    integrationInfo.updateDates
                )
            )
            createHippoViewData(store.getState())
        }
    }
}



