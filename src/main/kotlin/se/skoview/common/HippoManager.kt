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
    private val hippoStore = createReduxStore(
        ::hippoReducer,
        initializeHippoState()
    )

    fun initialize() {
        Pace.init()
        routing.initialize().resolve()

        val startUrl = window.location.href

        val view = parseUrlForView(startUrl)

        val bookmark = parseBookmarkString(startUrl)
        hippoStore.dispatch(HippoAction.ApplyBookmark(view, bookmark))

        GlobalScope.launch {

            hippoStore.dispatch(HippoAction.StartDownloadBaseItems)
            loadBaseItems()
            hippoStore.dispatch(HippoAction.DoneDownloadBaseItems)
            preSelectInitialize()

            if (hippoStore.getState().view == View.HIPPO) loadIntegrations(hippoStore.getState())
            else {
                if (hippoStore.getState().selectedPlattformChainsIds.isEmpty()) {
                    val tpId: Int? = Plattform.nameToId("SLL-PROD")
                    if (tpId != null) {
                        hippoStore.dispatch(HippoAction.StatTpSelected(tpId))
                    }
                }
                statSetPreselect("Alla konsumerande tjänster")
                // hippoStore.dispatch(HippoAction.SetPreselect(PreSelect.getDefault()))
                loadStatistics(hippoStore.getState())
            }
        }

        // actUponStateChangeInitialize()
    }

    fun Container.mainLoop() {
        // place for common header
        /*
        header(HippoManager.hippoStore) { state ->
            headerNav(state)
        }
         */
        // Called after each state change
        main(hippoStore) { state ->
            // setUrlFilter(state)
            println("In main()")
            if (state.downloadBaseItemStatus == AsyncActionStatus.COMPLETED) {
                when (state.view) {
                    View.HIPPO -> {
                        if (
                            state.downloadBaseItemStatus == AsyncActionStatus.COMPLETED &&
                            state.downloadIntegrationStatus == AsyncActionStatus.COMPLETED
                        ) {
                            hippoView(state)
                        }
                    }
                    View.STAT -> {
                        // todo: Refactor and clean up code below
                        /*
                        if (state.selectedPlattformChainsIds.isNotEmpty()) {
                            val pcId = state.selectedPlattformChainsIds[0]
                            val pc = PlattformChain.mapp[pcId]!!
                            val tp = Plattform.mapp[pc.last]!!
                            if (tp.name != "SLL-PROD") {
                                val tpId: Int? = Plattform.nameToId("SLL-PROD")
                                if (tpId != null) {
                                    hippoStore.dispatch(HippoAction.StatTpSelected(tpId))
                                }
                                loadStatistics(hippoStore.getState())
                            }
                        }
                        */
                        statView(state, View.STAT)
                    }
                    // View.STAT_ADVANCED -> statView(state, View.STAT_ADVANCED)
                }
            }
        }
        // footer()
    }

    fun newOrUpdatedUrlFromBrowser(view: View, params: String? = null) {
        println("¤¤¤¤¤¤¤¤¤¤¤¤ In fromUrl(), view=$view, params=$params")
        // setBaseUrl(view)
        val filterVals = params ?: ""
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
            else -> {
                val state = hippoStore.getState()
                if (state.downloadBaseItemStatus == AsyncActionStatus.COMPLETED)
                    if (state.isStatPlattformSelected())
                        loadStatistics(hippoStore.getState())
                    else
                        statTpSelected(Plattform.nameToId("SLL-PROD")!!)
            }
        }
    }

    fun dispatchProxy(action: HippoAction) {
        hippoStore.dispatch(action)
    }

    fun dateSelected(type: DateType, date: String) {
        val nextState = hippoStore.getState().dateSelected(date, type)
        navigateWithBookmark(nextState)
    }

    fun itemSelected(itemId: Int, type: ItemType) {
        val nextState = hippoStore.getState()
            .setShowAllItemTypes(true)
            .itemIdSeclected(itemId, type)
        navigateWithBookmark(nextState)
    }

    fun itemDeselected(itemId: Int, type: ItemType) {
        val nextState = hippoStore.getState()
            .itemIdDeseclected(itemId, type)
        navigateWithBookmark(nextState)
    }

    fun setViewMax(type: ItemType, lines: Int) {
        hippoStore.dispatch(HippoAction.SetVMax(type, lines))
    }

    fun statTpSelected(tpId: Int) {
        // hippoStore.dispatch(HippoAction.StatTpSelected(tpId))
        val nextState = hippoStore.getState().statTpSelected(tpId)
        navigateWithBookmark(nextState)
    }

    fun statHistorySelected(flag: Boolean) {
        if (flag) loadHistory(hippoStore.getState()) // Preload of history
        hippoStore.dispatch(HippoAction.ShowTimeGraph(flag))
    }

    fun statTechnicalTermsSelected(flag: Boolean) {
        hippoStore.dispatch(HippoAction.ShowTechnicalTerms(flag))
    }

    fun statShowConsumers(flag: Boolean) {
        hippoStore.dispatch(HippoAction.ShowConsumers(flag))
    }

    fun statShowProducers(flag: Boolean) {
        hippoStore.dispatch(HippoAction.ShowProduceras(flag))
    }

    fun statShowContracts(flag: Boolean) {
        hippoStore.dispatch(HippoAction.ShowContracts(flag))
    }

    fun statShowLogicalAddresses(flag: Boolean) {
        hippoStore.dispatch(HippoAction.ShowLogicalAddresses(flag))
    }

    fun statShowAllItemTypes(flag: Boolean) {
        hippoStore.dispatch(HippoAction.ShowAllItemTypes(flag))
    }

    fun setView(view: View) {
        console.log(hippoStore.getState())
        println("State change by setNewView")
        val nextState = hippoStore.getState().setNewView(view)
        console.log(nextState)
        navigateWithBookmark(nextState)
    }

    fun statSetPreselect(preSelectLabel: String) {
        // todo: Change to navigate call
        val preSelect: PreSelect? = PreSelect.mapp[preSelectLabel]
        // ?: throw NullPointerException("Internal error in Select View")
        val nextState = hippoStore.getState().setPreselect(preSelect)
        // hippoStore.dispatch(HippoAction.SetPreselect(preSelect))
        navigateWithBookmark(nextState)
        // loadStatistics(hippoStore.getState())
    }

    private fun navigateWithBookmark(nextState: HippoState) {
        val bookmarkString = nextState.createBookmarkString()
        println("In navigateWithBookmark, bookmarkString = '$bookmarkString'")
        console.log(nextState)
        val route: String =
            if (bookmarkString.isNotEmpty()) "/filter=$bookmarkString"
            else ""
        val newView = nextState.view
        routing.navigate(newView.url + route)
    }

    private fun parseUrlForView(url: String): View {
        if (url.contains("integrationer.tjansteplattform")) return View.HIPPO
        if (url.contains("statistik.tjansteplattform")) return View.STAT

        if (url.contains(View.STAT.url)) return View.STAT
        // if (url.contains(View.STAT_ADVANCED.url)) return View.STAT_ADVANCED

        return View.HIPPO
    }
}
