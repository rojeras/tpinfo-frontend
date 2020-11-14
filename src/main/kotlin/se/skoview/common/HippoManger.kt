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

import pl.treksoft.kvision.redux.createReduxStore
import pl.treksoft.navigo.Navigo

object HippoManager { // } : CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()) {

    private val routing = Navigo(null, true, "#")

    // var downloadBaseItemsJob: Deferred<Unit> = Job()

    val hippoStore = createReduxStore(
        ::hippoReducer,
        initializeHippoState()
    )

    fun initialize() {
        println("In Manager initialize()")
        routing.initialize().resolve()
        loadBaseItems(hippoStore)
        println("Back in Manager, will now loadIntegrations()")
        loadIntegrations(hippoStore.getState())
        println("Exiting Manager initialize()")
    }

    fun fromUrl(view: View, params: String? = null) {
        if (hippoStore.getState().downloadIntegrationStatus != AsyncActionStatus.COMPLETED) {
            println("Wating for baseitem compeltion")
            // downloadBaseItemsJob.await()
        }
        println("Time to view view")
        if (view == View.HIPPO) {
            loadIntegrations(hippoStore.getState())
        }

        println("In fromUrl(), view=$view, params=$params")
    }
}
