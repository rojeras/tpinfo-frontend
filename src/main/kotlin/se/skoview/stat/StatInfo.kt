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
package se.skoview.stat

import kotlinx.serialization.Serializable
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import se.skoview.common.* // ktlint-disable no-wildcard-imports

@Serializable
val statArrArrCache: HashMap<String, Array<Array<Int>>> = hashMapOf<String, Array<Array<Int>>>()

data class StatisticsBlob(
    val statisticsArrArr: Array<Array<Int>>

) {
    val callsConsumer = mutableMapOf<Int, Int>()
    val callsProducer = mutableMapOf<Int, Int>()
    val callsLogicalAddress = mutableMapOf<Int, Int>()
    val callsDomain = mutableMapOf<Int, Int>()
    val callsContract = mutableMapOf<Int, Int>()

    init {

        for (arr in statisticsArrArr) {
            if (arr.size > 1) { // Needed during the first initialization of the store
                updateCallsArr(callsLogicalAddress, arr[1], arr[6])
                updateCallsArr(callsContract, arr[2], arr[6])
                updateCallsArr(callsDomain, arr[3], arr[6])
                updateCallsArr(callsConsumer, arr[4], arr[6])
                updateCallsArr(callsProducer, arr[5], arr[6])
            }
        }
    }
}

fun loadStatistics(state: HippoState) {
    HippoManager.dispatchProxy(HippoAction.StartDownloadStatistics)

    val urlParameters = state.getParams(state.view)
    val parameters = "statistics$urlParameters"

    // Check if the statistics info is available in the cache
    if (statArrArrCache.containsKey(parameters)) {
        println(">>> Statistics found in cache, parameters: $parameters")
        val statisticsArrArr = statArrArrCache[parameters]!!

        HippoManager.dispatchProxy(HippoAction.DoneDownloadStatistics(statisticsArrArr))
        loadHistory(state)
    } else {
        println(">>> Statistics data NOT found in cache - will now download, parameters: $parameters")
        console.log(parameters)

        getAsyncTpDb(parameters) { response ->
            println(">>> Size of fetched statistics data is: ${response.length}")
            val statisticsArrArr: Array<Array<Int>> = JSON.parse(response)

            statArrArrCache[parameters] = statisticsArrArr
            HippoManager.dispatchProxy(HippoAction.DoneDownloadStatistics(statisticsArrArr))
            loadHistory(state)
        }
    }
    // if (state.showTimeGraph) loadHistory(state)
}

private fun updateCallsArr(callsArr: MutableMap<Int, Int>, itemId: Int, calls: Int) {
    if (callsArr[itemId] == null) callsArr[itemId] = 0
    callsArr[itemId] = callsArr[itemId]!!.plus(calls)
}

fun exportStatData(state: HippoState) {

    val selectedPlattformChainId = state.selectedPlattformChainsIds[0]
    val selectedTpId = PlattformChain.mapp[selectedPlattformChainId]!!.last
    val selectedTpName = Plattform.mapp[selectedTpId]!!.name

    // val selectedContractList: List<String> = state.selectedContracts.map { ServiceContract.map[it]!!.name }

    var csvData: String = """
     Informationen är baserad på följande filtrering:
     
     Från datum: ${state.dateEffective}
     Till datum: ${state.dateEnd}
     Aktuell tjänsteplattform: $selectedTpName
     
    """.trimIndent()

    if (state.selectedConsumersIds.size > 0)
        csvData += "Tjänstekonsumenter: ${
        state.selectedConsumersIds.map { ServiceComponent.mapp[it]!!.name }.joinToString()
        }\n"

    if (state.selectedContractsIds.size > 0)
        csvData += "Tjänstekontrakt: ${
        state.selectedContractsIds.map { ServiceContract.mapp[it]!!.name }.joinToString()
        } \n"

    if (state.selectedLogicalAddressesIds.size > 0)
        csvData +=
            "Logiska adresser : ${
            state.selectedLogicalAddressesIds.map { LogicalAddress.mapp[it]!!.name }.joinToString()
            }\n"

    if (state.selectedProducersIds.size > 0)
        csvData += "Tjänsteproducenter: ${
        state.selectedProducersIds.map { ServiceComponent.mapp[it]!!.name }.joinToString()
        }\n"

    csvData += "\n"
    csvData += "consumerHsa; consumerDescription; calls; plattform; domain; contract; logicalAddress; logicalAddressDescription; producerHsa; producerDescription;\n"

    // todo: Need to save the statisticsArrArr and use to produce the data in the CSV file
    for (arr in state.statBlob.statisticsArrArr) {
        val logicalAddress = LogicalAddress.mapp[arr[1]]!!
        val contract = ServiceContract.mapp[arr[2]]!!
        val domain = ServiceDomain.mapp[arr[3]]!!
        val consumer = ServiceComponent.mapp[arr[4]]!!
        val producer = ServiceComponent.mapp[arr[5]]!!
        val calls = arr[6]

        csvData += "${consumer.hsaId}; ${consumer.description};"
        csvData += "$calls;"
        csvData += "$selectedTpName;"
        csvData += "${domain.name};"
        csvData += "${contract.name} ${contract.major};"
        csvData += "${logicalAddress.name}; ${logicalAddress.description};"
        csvData += "${producer.hsaId}; ${producer.description};"
        csvData += "\n"
    }

    val fileSaver = pl.treksoft.kvision.require("file-saver")
    val BOM = "\uFEFF"
    val csv = Blob(arrayOf(BOM + csvData), BlobPropertyBag("text/csv;charset=utf-8"))
    fileSaver.saveAs(csv, "tp-anropsstatistik.csv")
}
