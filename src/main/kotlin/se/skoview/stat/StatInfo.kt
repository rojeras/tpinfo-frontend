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

import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import se.skoview.app.store
import se.skoview.common.*

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
        println("Number of statistics records: ${statisticsArrArr.size}")
    }
}


fun loadStatistics(state: HippoState) {
    val urlParameters = state.getParams()
    val parameters = "statistics$urlParameters"

    // Check if the statistics info is available in the cache
    if (statArrArrCache.containsKey(parameters)) {
        println("Statistics found in cache")
        val statisticsArrArr = statArrArrCache[parameters]!!

        store.dispatch(
            HippoAction.DoneDownloadStatistics(statisticsArrArr)
        )
    } else {
        println(">>> Statistics data NOT found in cache - will now download")
        console.log(parameters)
        getAsyncTpDb(parameters) { response ->
            println(">>> Size of fetched statistics data is: ${response.length}")

            val statisticsArrArr: Array<Array<Int>> = JSON.parse(response)
            statArrArrCache[parameters] = statisticsArrArr
            store.dispatch(HippoAction.DoneDownloadStatistics(statisticsArrArr))
        }
    }
    if (state.showTimeGraph) loadHistory(state)

}

private fun updateCallsArr(callsArr: MutableMap<Int, Int>, itemId: Int, calls: Int) {
    if (callsArr[itemId] == null) callsArr[itemId] = 0
    callsArr[itemId] = callsArr[itemId]!!.plus(calls)
}

fun exportStatData(state: HippoState) {

    val selectedPlattformChainId = state.selectedPlattformChains[0]
    val selectedTpId = PlattformChain.map[selectedPlattformChainId]!!.last
    val selectedTpName = Plattform.map[selectedTpId]!!.name

    val selectedContractList: List<String> = state.selectedContracts.map { ServiceContract.map[it]!!.name }

    var csvData = """
     Informationen är baserad på följande filtrering:
     
     Från datum: ${state.dateEffective}
     Till datum: ${state.dateEnd}
     Aktuell tjänsteplattform: $selectedTpName
     
    """.trimIndent()

    if (state.selectedConsumers.size > 0)
        csvData += "Tjänstekonsumenter: ${
            state.selectedConsumers.map { ServiceComponent.map[it]!!.name }.joinToString()
        }\n"

    if (state.selectedContracts.size > 0)
        csvData += "Tjänstekontrakt: ${
            state.selectedContracts.map { ServiceContract.map[it]!!.name }.joinToString()
        } \n"

    if (state.selectedLogicalAddresses.size > 0)
        csvData +=
            "Logiska adresser : ${
                state.selectedLogicalAddresses.map { LogicalAddress.map[it]!!.name }.joinToString()
            }\n"

    if (state.selectedProducers.size > 0)
        csvData += "Tjänsteproducenter: ${
            state.selectedProducers.map { ServiceComponent.map[it]!!.name }.joinToString()
        }\n"

    csvData += "\n"
    csvData += "consumerHsa; consumerDescription; calls; plattform; domain; contract; logicalAddress; logicalAddressDescription; producerHsa; producerDescription;\n"

    // todo: Need to save the statisticsArrArr and use to produce the data in the CSV file
    for (arr in state.statBlob.statisticsArrArr) {
        val logicalAddress = LogicalAddress.map[arr[1]]!!
        val contract = ServiceContract.map[arr[2]]!!
        val domain = ServiceDomain.map[arr[3]]!!
        val consumer = ServiceComponent.map[arr[4]]!!
        val producer = ServiceComponent.map[arr[5]]!!
        val calls = arr[6]

        csvData += "${consumer.hsaId}; ${consumer.description};"
        csvData += "$calls;"
        csvData += "${selectedTpName};"
        csvData += "${domain.name};"
        csvData += "${contract.name} ${contract.major};"
        csvData += "${logicalAddress.name}; ${logicalAddress.description};"
        csvData += "${producer.hsaId}; ${producer.description};"
        csvData += "\n"
    }

    val fileSaver = pl.treksoft.kvision.require("file-saver")
    val csv =
        Blob(arrayOf(csvData), BlobPropertyBag("text/csv;charset=utf-8,\uFEFF"))
    fileSaver.saveAs(csv, "tp-anropsstatistik.csv")
}