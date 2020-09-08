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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import se.skoview.app.store
import se.skoview.common.HippoAction
import se.skoview.common.HippoState
import se.skoview.common.getAsyncTpDb
import se.skoview.common.getParams

@Serializable
data class HistoryEntry(
    val aDate: String,
    val numberOf: Int
)

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
        println(">>> History data found in cache")
        store.dispatch(
            HippoAction.DoneDownloadHistory(HistoryCache.map[parameters]!!.historyMap)
        )
    } else {
        println(">>> History data NOT found in cache - will download")
        console.log(parameters)
        /* Orginalversion som fungerar
                    val json = Json(JsonConfiguration.Stable)
            val history = json.parse(HistoryInfo.serializer(), response)
         */
        getAsyncTpDb(parameters) { response ->
            println(">>> Size of fetched history data is: ${response.length}")
            //val json = Json(JsonConfiguration.Stable)
            val history = JSON.parse<HistoryInfo>(response)
            console.log(history.history)
            val historyMap = mutableMapOf<String, Int>()
            for (item in history.history) {
                println("Varv")
                //println(item)
            }
            /*
            // Store in cache
            HistoryCache(
                parameters,
                history.history
            )
             */

            println("Time to Dispatch")
            /*
            store.dispatch(
                HippoAction.DoneDownloadHistory(HistoryCache.map[parameters]!!.historyMap)
            )
             */
        }
    }
}

