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
import io.kvision.rest.HttpMethod
import io.kvision.rest.RestClient

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
    val urlParameters = state.getParams(state.view)
    val parameters = "integrations$urlParameters"

    HippoManager.dispatchProxy(HippoAction.StartDownloadIntegrations)

    if (IntegrationCache.map.containsKey(parameters)) {
        val integrationsCache = IntegrationCache.map[parameters]
        // todo: Make sure to remove the !! below
        HippoManager.dispatchProxy(
            HippoAction.DoneDownloadIntegrations(
                integrationsCache!!.integrationArr,
                integrationsCache.maxCounters,
                integrationsCache.updateDates
            )
        )
    } else {

        val restClient = RestClient()

        val url = "${tpdbBaseUrl()}$parameters"
        println(url)

        GlobalScope.launch {
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

            HippoManager.dispatchProxy(
                HippoAction.DoneDownloadIntegrations(
                    integrationArrs,
                    integrationInfo.maxCounters,
                    integrationInfo.updateDates
                )
            )
        }
    }
}



