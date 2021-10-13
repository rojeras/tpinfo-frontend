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
package se.skoview.controller

import io.kvision.core.Container
import io.kvision.html.main
import io.kvision.navigo.Navigo
import io.kvision.pace.Pace
import io.kvision.redux.createReduxStore
import io.kvision.state.bind
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import se.skoview.model.*
import se.skoview.view.hippo.ItemType
import se.skoview.view.hippo.hippoView
import se.skoview.view.hippo.loadIntegrations
import se.skoview.view.stat.PreSelect
import se.skoview.view.stat.loadStatistics
import se.skoview.view.stat.preSelectInitialize
import se.skoview.view.stat.statPage

/**
 * The central manager.
 *
 * HippoManager manages:
 * 1. Initialization of the different parts of the applications.
 * 1. URL and navigation
 * 1. Bookmarks
 * 1. All dispatch of state updates
 */
object HippoManager { // } : CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()) {

    private val routing = Navigo(null, true, "#")

    // todo: Move remaining dispatch to HippoManager and make the hippoStore private
    private val hippoStore = createReduxStore(
        ::hippoReducer,
        initializeHippoState()
    )

    /**
     * Initialize the hippo or statistics application. It loads the base information, and then integrations or statistics depending on the URL.
     */
    fun initialize() {

        Pace.init()
        routing.initialize().resolve()

        val startUrl = window.location.href
        println("In HippoManager.initialize(), startUrl = $startUrl")

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
                loadStatistics(hippoStore.getState())
            }
        }
    }

    /**
     * Extension function to Contiainer.
     *
     * It defines the main() which is defined as part of KVision and called at each state change.
     */
    fun Container.mainLoop() {
        // place for common header
        println("In mainLoop()")

        // Called after each state change
        main().bind(hippoStore) { state ->
            println("In main()")
            if (state.downloadBaseItemStatus == AsyncActionStatus.COMPLETED) {
                when (state.view) {
                    View.HOME -> println("Got View.HOME in main()")
                    View.HIPPO -> {
                        if ( // This if-construct should be possible to remove
                            state.downloadBaseItemStatus == AsyncActionStatus.COMPLETED // &&
                        ) {
                            hippoView(state)
                        }
                    }
                    View.STAT -> {
                        statPage(state)
                    }
                }
            }
        }
        // footer()
    }

    /**
     * Central part of routing. Entry point when browser URL changes.
     *
     * When the URL is changed, for example the bookmark of the filter parameter, this function is invoked.
     * It is invoked by [Navigo.initialize]
     *
     * @param view Requested view
     * @param params The parameters of the URL
     * @param origin The original URL structure, see [Navigo.initialize]
     */
    fun newOrUpdatedUrlFromBrowser(
        view: View,
        params: String? = null,
        origin: String = ""
    ) {
        println("¤¤¤ In fromUrl(), view=$view, params=$params, origin=$origin")
        val fullUrl = window.location.href
        println("URL is: $fullUrl")

        // If no params then we might have an old saved hippo link with a filter parameter in the wrong place
        // Check the whole URL
        val filter: String = if (params == null) fullUrl
        else params

        val bookmark = parseBookmarkString(filter)
        println("bookmark from filter:")
        console.log(bookmark)

        val oldState = hippoStore.getState()
        hippoStore.dispatch(HippoAction.ApplyBookmark(view, bookmark))
        val newState = hippoStore.getState()

        val isIntegrationSelectionsChanged: Boolean = newState.isIntegrationSelectionsChanged(oldState)
        if (isIntegrationSelectionsChanged) println("Update integrations")
        else println("Do NOT update integrations")

        val isStatisticsSelectionsChanged = newState.isStatisticsSelectionsChanged(oldState)
        if (isStatisticsSelectionsChanged) println("Update statistics")
        else println("Do NOT update statistics")

        when (view) {
            View.HOME -> routing.navigate(View.HIPPO.url)
            View.HIPPO -> {
                document.title = "hippo v7"
                if (
                    newState.downloadBaseDatesStatus == AsyncActionStatus.COMPLETED &&
                    newState.isIntegrationSelectionsChanged(oldState)
                )
                    loadIntegrations(hippoStore.getState())
            }
            View.STAT -> {
                document.title = "Statistik v7"
                if (newState.downloadBaseItemStatus == AsyncActionStatus.COMPLETED) {
                    if (newState.isStatPlattformSelected()) {
                        if (
                            newState.isStatisticsSelectionsChanged(oldState) ||
                            oldState.view != View.STAT
                        ) {
                            loadStatistics(hippoStore.getState())
                        }
                    } else statTpSelected(Plattform.nameToId("SLL-PROD")!!)
                }
            }
        }
    }

    /**
     * Generic facade to dispatch redux actions.
     *
     * @param action Action to dispatch, see [HippoAction]
     */
    fun dispatchProxy(action: HippoAction) {
        hippoStore.dispatch(action)
    }

    /**
     * Facade to dispatch
     *
     * Specify it synonyms or the technical terms should be displayed (in Statistik). Since [navigateWithBookmark]
     * is not used this action will not be represented in the URL and browser history.
     *
     * This function could be replaced by [dispatchProxy] and kept here as a preparation to include this setting in
     * the browser history.
     *
     * @param flag Show technical terms (or synonyms)
     */
    fun statTechnicalTermsSelected(flag: Boolean) {
        hippoStore.dispatch(HippoAction.ShowTechnicalTerms(flag))
        // val nextState = hippoStore.getState().setFlag(HippoAction.ShowTechnicalTerms(flag))
        // navigateWithBookmark(nextState)
    }

    /**
     * Heart of the routing/navigation algorithm.
     *
     * The function is called with a target state object, that is, the next state the application is going
     * to apply. It is done through the browser URL. A URL (really the bookmakrk part of the URL) is
     * created, and set in the browser by the [routing].navigate() function. That will invoke the
     * [newOrUpdatedUrlFromBrowser] which will parse the URL and do the actual state update.
     *
     * This seemingly cumbersome way of switching state ensures that the URL match the state, and also enables browser
     * history and the use of the backward and forward browser buttons.
     *
     * @param nextState A state object representing the next state the application shoould apply.
     */
    private fun navigateWithBookmark(nextState: HippoState) {
        val bookmarkString = nextState.createBookmarkString()
        println("In navigateWithBookmark, bookmarkString = '$bookmarkString', nextState:")
        console.log(nextState)
        val route: String =
            if (bookmarkString.isNotEmpty()) "/filter=$bookmarkString"
            else ""
        val newView = nextState.view
        routing.navigate(newView.url + route)
    }

    /**
     * Navigate function which creates an updated state object and calls [navigateWithBookmark] to select a date.
     *
     * @param type Type of date
     * @param date New date
     */
    fun dateSelected(type: DateType, date: String) {
        val nextState = hippoStore.getState()
            .dateSelected(date, type)
        navigateWithBookmark(nextState)
    }

    /**
     * Navigate function which creates an updated state object and calls [navigateWithBookmark] to select an item.
     *
     * @param itemId The id of the selected item
     * @param type Type of the selected item
     */
    fun itemSelected(itemId: Int, type: ItemType) {
        val nextState = hippoStore.getState()
            .setShowAllItemTypes(true)
            .itemIdSeclected(itemId, type)
        navigateWithBookmark(nextState)
    }

    /**
     * Navigate function which creates an updated state object and calls [navigateWithBookmark] to deselect an item.
     *
     * @param itemId The id of the deselected item
     * @param type Type of the deselected item
     */
    fun itemDeselected(itemId: Int, type: ItemType) {
        println("Deselect item, state:")
        console.log(hippoStore.getState())
        val nextState = hippoStore.getState()
            .itemIdDeseclected(itemId, type)
        navigateWithBookmark(nextState)
    }

    /**
     * Navigate function which creates an updated state object and calls [navigateWithBookmark] to toggle a selection of an item
     *
     * @param itemId Id of item which shuould toogle its selection status
     * @param itemType Type of the item
     */
    fun itemSelectDeselect(itemId: Int, itemType: ItemType) {
        if (hippoStore.getState().isItemSelected(itemType, itemId)) {
            itemDeselected(itemId, itemType)
        } else {
            itemSelected(itemId, itemType)
        }
    }

    /**
     * Navigate function which creates an updated state object and calls [navigateWithBookmark] to set the maximum number of items to show in a certain column (in hippo)
     *
     * @param type Type of the items in the column
     * @param lines Number of items to show in the column
     */
    fun setViewMax(type: ItemType, lines: Int) {
        hippoStore.dispatch(HippoAction.SetVMax(type, lines))
    }

    /**
     * Navigate function which creates an updated state object and calls [navigateWithBookmark] to select a platform to show (in Statistik)
     *
     * @param tpId Id of the plattform
     */
    fun statTpSelected(tpId: Int) {
        // hippoStore.dispatch(HippoAction.StatTpSelected(tpId))
        val nextState = hippoStore.getState().statTpSelected(tpId)
        navigateWithBookmark(nextState)
    }

    /*
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
*/
    /**
     * Navigate function which creates an updated state object and calls [navigateWithBookmark] for slection in Statistik to show all items types.
     *
     */
    fun statShowAllItemTypes() {
        // hippoStore.dispatch(HippoAction.ShowAllItemTypes(flag))
        val nextState = hippoStore.getState()
            .setShowAllItemTypes(true)
        navigateWithBookmark(nextState)
    }

    /**
     * Navigate function which creates an updated state object and calls [navigateWithBookmark] with flag to display the history chart.
     *
     * @param flag Show history chart or not
     */
    fun statHistorySelected(flag: Boolean) {
        // if (flag) loadHistory(hippoStore.getState()) // Preload of history
        // hippoStore.dispatch(HippoAction.ShowTimeGraph(flag))
        val nextState = hippoStore.getState()
            .setFlag(HippoAction.ShowTimeGraph(flag)) // .setShowAllItemTypes(true)
        navigateWithBookmark(nextState)
    }

    /**
     * Navigate function which creates an updated state object and calls [navigateWithBookmark] to switch between hippo and Statistik.
     *
     * @param view Which view (application) should be visible
     */
    fun setView(view: View) {
        val nextState = hippoStore.getState()
            .setNewView(view)
        console.log(nextState)
        navigateWithBookmark(nextState)
    }

    /**
     * Navigate function which creates an updated state object and calls [navigateWithBookmark] to apply a preselect (to the Statistik)
     *
     * @param preSelectLabel Label which identifies a preSelect
     */
    fun statSetPreselect(preSelectLabel: String) {
        val preSelect: PreSelect? = PreSelect.mapp[preSelectLabel]
        val nextState =
            hippoStore.getState()
                .setPreselect(preSelect)
                .setFlag(HippoAction.ShowTimeGraph(false))
        navigateWithBookmark(nextState)
    }
}
