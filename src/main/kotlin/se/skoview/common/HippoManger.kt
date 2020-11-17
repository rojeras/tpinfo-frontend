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

import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pl.treksoft.kvision.redux.createReduxStore
import pl.treksoft.navigo.Navigo

object HippoManager { // } : CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()) {

    private val routing = Navigo(null, true, "#")

    val hippoStore = createReduxStore(
        ::hippoReducer,
        initializeHippoState()
    )

    fun initialize() {
        println("In Manager initialize()")
        routing.initialize().resolve()

        val startUrl = window.location.href
        println("window.location.href: $startUrl")

        GlobalScope.launch {
            println("Will start to load dates")
            loadBaseItem("dates", Dates.serializer())
            println("--- Dates loaded")
            hippoStore.dispatch(HippoAction.SetDownloadBaseDatesStatus(AsyncActionStatus.COMPLETED))

            hippoStore.dispatch(HippoAction.StartDownloadBaseItems)
            loadBaseItems(hippoStore)
            hippoStore.dispatch(HippoAction.DoneDownloadBaseItems)
        }

        // Initialize the state change listener in the manager
        actUponStateChangeInitialize()

        println("""Exiting Manager initialize()""")
    }

    /**
     * This function is part of the manager. It listen to state changes, evaluates them and,
     * in some cases, act.
     */
    private fun actUponStateChangeInitialize() {
        hippoStore.subscribe { state ->
            println("--- In actUponStateChange() - subscribe")
            if (state.view == View.HIPPO) {
                // Load integrations if not loaded and dates are available
                if (
                    state.downloadIntegrationStatus == AsyncActionStatus.NOT_INITIALIZED &&
                    state.downloadBaseDatesStatus == AsyncActionStatus.COMPLETED
                ) {
                    println("Will now loadIntegrations()")
                    loadIntegrations(hippoStore.getState())
                }
            }
        }
    }

    fun newOrUpdatedUrlFromBrowser(view: View, params: String? = null) {
        println("¤¤¤¤¤¤¤¤¤¤¤¤ In fromUrl(), view=$view, params=$params")
        hippoStore.dispatch(HippoAction.SetView(view))
        when (view) {
            // If HOME go to HIPPO
            View.HOME -> routing.navigate(View.HIPPO.url)
            View.HIPPO -> {
                println("HIPPO:" + params)

                val filterVals = if (params != null) params else ""
                val bookmark = parseBookmark(filterVals)
                println("bookmark from filter:")
                console.log(bookmark)
                hippoStore.dispatch(HippoAction.ApplyBookmark(View.HIPPO, bookmark))
                // Ensure default dates are available before load of integrations
                // If not, load will be initialized by function actUponStateChangeInitialize()
                if (hippoStore.getState().downloadBaseDatesStatus == AsyncActionStatus.COMPLETED)
                    loadIntegrations(hippoStore.getState())
            }
            View.STAT_SIMPLE -> println("STAT_SIMPLE:" + params)
            View.STAT_ADVANCED -> println("STAT_ADVANCED:" + params)
        }
    }

    fun bothDatesSelected(date: String) {
        hippoStore.dispatch(HippoAction.DateSelected(DateType.EFFECTIVE_AND_END, date))
        navigateWithBookmark()
    }

    fun itemSelected(item: BaseItem, type: ItemType) {
        hippoStore.dispatch(HippoAction.ItemIdSelected(type, item.id))
        navigateWithBookmark()
    }

    fun itemDeselected(item: BaseItem, type: ItemType) {
        hippoStore.dispatch(HippoAction.ItemIdDeselected(type, item.id))
        navigateWithBookmark()
    }

    fun setViewMax(type: ItemType, lines: Int) {
        hippoStore.dispatch(HippoAction.SetVMax(type, lines))
    }

    private fun navigateWithBookmark() {
        val bookmark = hippoStore.getState().getBookmark()
        routing.navigate(View.HIPPO.url + "/filter=$bookmark")
    }
}
