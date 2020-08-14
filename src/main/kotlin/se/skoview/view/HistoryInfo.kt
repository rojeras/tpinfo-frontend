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
package se.skoview.view

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import se.skoview.app.store
import se.skoview.data.HippoAction
import se.skoview.data.HippoState
import se.skoview.data.getParams
import se.skoview.lib.getAsyncTpDb

@Serializable
data class HistoryInfo(
    val history: Map<String, Int>
)

data class HistoryCache(
    val key: String,
    val historyMap: Map<String, Int>

    ) {
    init {
        map[key] = this
    }

    companion object {
        val map: HashMap<String, HistoryCache> = hashMapOf<String, HistoryCache>()
    }
}

fun loadHistory(state: HippoState) {
    val urlParameters = state.getParams()
    val parameters = "history$urlParameters"

    // Check if the statistics info is available in the cache
    if (HistoryCache.map.containsKey(parameters)) {
        println("History found in cache")
        val historyCache = HistoryCache.map[parameters]!!

        store.dispatch { _, _ ->
            HippoAction.DoneDownloadHistory(historyCache.historyMap)
        }
        /*
        store.dispatch { _, _ ->
            store.dispatch(
                HippoAction.DoneDownloadHistory(
                    historyCache.historyMap
                )
            )
            //SInfo.createStatViewData(getState())
        }
        */
    } else {
        println(">>> History data NOT found in cache - will download")
        console.log(parameters)
        getAsyncTpDb(parameters) { response ->
            println(">>> Size of fetched history data is: ${response.length}")

            val json = Json(JsonConfiguration.Stable)
            val history = json.parse(HistoryInfo.serializer(), response)

            for ((key, value ) in history.history) {
                println("$key : $value")
            }

            // Store in cache
            HistoryCache(
                parameters,
                history.history
            )

            println("time to dispatch")
            store.dispatch(
                HippoAction.DoneDownloadHistory(history.history)
            )
            //console.log(history)

            //println("History records: ${historyList[0].history}")

            /*
            store.dispatch { _, _ ->
                store.dispatch(
                    HippoAction.DoneDownloadStatistics(
                        statisticsCache.historyMap,
                        statisticsCache.callsProducer,
                        statisticsCache.callsLogicalAddress,
                        statisticsCache.callsDomain,
                        statisticsCache.callsContract
                    )
                )
                //SInfo.createStatViewData(getState())
            }
             */
        }
    }

}

