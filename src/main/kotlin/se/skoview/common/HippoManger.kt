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
import pl.treksoft.kvision.core.Container
import pl.treksoft.kvision.html.main
import pl.treksoft.kvision.pace.Pace
import pl.treksoft.kvision.redux.createReduxStore
import pl.treksoft.navigo.Navigo
import se.skoview.hippo.hippoView
import se.skoview.stat.* // ktlint-disable no-wildcard-imports

object HippoManager { // } : CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()) {

    private val routing = Navigo(null, true, "#")

    // todo: Move remaining dispatch to HippoManager and make the hippoStore private
    val hippoStore = createReduxStore(
        ::hippoReducer,
        initializeHippoState()
    )

    fun initialize() {
        println("In Manager initialize()")
        Pace.init()
        routing.initialize().resolve()

        val startUrl = window.location.href
        println("window.location.href: $startUrl")

        val view = parseUrlForView(startUrl)
        val bookmark = parseBookmarkString(startUrl)
        hippoStore.dispatch(HippoAction.ApplyBookmark(view, bookmark))

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
            } else if (
                state.view == View.STAT_SIMPLE ||
                state.view == View.STAT_ADVANCED
            ) {
                // Load statistics data if not loaded and dates are available
                if (
                    state.downloadStatisticsStatus == AsyncActionStatus.NOT_INITIALIZED
                ) {
                    println("Will now loadStatistics()")
                    loadStatistics(hippoStore.getState())
                }
            }
        }
    }

    fun Container.mainLoop() {
        // place for common header
        /*
        header(HippoManager.hippoStore) { state ->
            headerNav(state)
        }
         */
        main(HippoManager.hippoStore) { state ->
            // setUrlFilter(state)
            if (state.downloadBaseItemStatus == AsyncActionStatus.COMPLETED) {
                when (state.view) {
                    View.HOME -> println("View.HOME found in main")
                    View.HIPPO -> {
                        if (
                            state.downloadBaseItemStatus == AsyncActionStatus.COMPLETED &&
                            state.downloadIntegrationStatus == AsyncActionStatus.COMPLETED
                        ) {
                            hippoView(state)
                        }
                    }
                    View.STAT_SIMPLE -> statView(state, View.STAT_SIMPLE)
                    View.STAT_ADVANCED -> statView(state, View.STAT_ADVANCED)
                }
            }
        }
        // footer()
    }

    fun newOrUpdatedUrlFromBrowser(view: View, params: String? = null) {
        println("¤¤¤¤¤¤¤¤¤¤¤¤ In fromUrl(), view=$view, params=$params")
        val filterVals = if (params != null) params else ""
        val bookmark = parseBookmarkString(filterVals)
        println("bookmark from filter:")
        console.log(bookmark)
        hippoStore.dispatch(HippoAction.ApplyBookmark(view, bookmark))
        // hippoStore.dispatch(HippoAction.SetView(view))
        when (view) {
            // If HOME go to HIPPO
            View.HOME -> routing.navigate(View.HIPPO.url)
            View.HIPPO -> {
                if (hippoStore.getState().downloadBaseDatesStatus == AsyncActionStatus.COMPLETED)
                    loadIntegrations(hippoStore.getState())
            }
            View.STAT_SIMPLE -> loadStatistics(hippoStore.getState())
            View.STAT_ADVANCED -> loadStatistics(hippoStore.getState())
        }
    }

    fun dateSelected(type: DateType, date: String) {
        hippoStore.dispatch(HippoAction.DateSelected(type, date))
        // navigateWithBookmark()
    }

    fun itemSelected(item: BaseItem, type: ItemType) {
        val preState = hippoStore.getState().itemIdSeclected(item.id, type)
        navigateWithBookmark(preState)
    }

    fun itemDeselected(item: BaseItem, type: ItemType) {
        val preState = hippoStore.getState().itemIdDeseclected(item.id, type)
        navigateWithBookmark(preState)
    }

    fun setViewMax(type: ItemType, lines: Int) {
        hippoStore.dispatch(HippoAction.SetVMax(type, lines))
    }

    fun statTpSelected(tpId: Int) {
        val preState = hippoStore.getState().statTpSelected(tpId)
        navigateWithBookmark(preState)
    }

    fun statHistorySelected(flag: Boolean) {
        if (flag) loadHistory(hippoStore.getState()) // Preload of history
        hippoStore.dispatch(HippoAction.ShowTimeGraph(flag))
    }

    fun statTechnialTermsSelected(flag: Boolean) {
        hippoStore.dispatch(HippoAction.ShowTechnicalTerms(flag))
    }

    fun setView(view: View) {
        val preState = hippoStore.getState().setView(view)
        navigateWithBookmark(preState)
        // hippoStore.dispatch(HippoAction.SetView(mode)) // todo: Change to navigate call
        // loadStatistics(hippoStore.getState())
    }

    fun statSetViewModePreselect(preSelectLabel: String) {
        // todo: Change to navigate call
        when (hippoStore.getState().view) {
            View.STAT_SIMPLE -> {
                val preSelect = SimpleViewPreSelect.mapp[preSelectLabel]
                    ?: throw NullPointerException("Internal error in Select View")
                hippoStore.dispatch(HippoAction.SetSimpleViewPreselect(preSelect))
            }
            View.STAT_ADVANCED -> {
                val preSelect = AdvancedViewPreSelect.mapp[preSelectLabel]
                    ?: throw NullPointerException("Internal error in Select View")
                hippoStore.dispatch(HippoAction.SetAdvancedViewPreselect(preSelect))
            }
        }
        loadStatistics(hippoStore.getState())
    }

    private fun navigateWithBookmark(preState: HippoState) {
        val bookmarkString = preState.createBookmarkString()
        val route: String =
            if (bookmarkString.isNotEmpty()) "/filter=$bookmarkString"
            else ""
        val currentView = hippoStore.getState().view
        routing.navigate(currentView.url + route)
    }

    private fun parseUrlForView(url: String): View {
        if (url.contains("integrationer.tjansteplattform")) return View.HIPPO
        if (url.contains("statistik.tjansteplattform")) return View.STAT_SIMPLE

        if (url.contains(View.STAT_SIMPLE.url)) return View.STAT_SIMPLE
        if (url.contains(View.STAT_ADVANCED.url)) return View.STAT_ADVANCED

        return View.HIPPO
    }
}
