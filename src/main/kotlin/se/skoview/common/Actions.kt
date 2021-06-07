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

import io.kvision.redux.RAction
import se.skoview.stat.PreSelect

/**
 * Definition of the *redux* actions used in *frontend*
 */

sealed class HippoAction : RAction {
    /**
     * Select view (hippo or stat).
     */
    data class SetView(val view: View) : HippoAction()

    /**
     * The base dates must be downloaded before the first integration download, and gets its own status.
     */
    data class SetDownloadBaseDatesStatus(val status: AsyncActionStatus) : HippoAction()
    object StartDownloadBaseItems : HippoAction()
    object DoneDownloadBaseItems : HippoAction()
    data class ErrorDownloadBaseItems(val errorMessage: String) : HippoAction()
    object StartDownloadIntegrations : HippoAction()

    /**
     * Integration contains three different JSON structures
     */
    data class DoneDownloadIntegrations(
        /**
         * A matrix of integration specifications (based on item ids)
         */
        val integrationArrs: List<Integration>,
        /**
         * The total number of each type for this specific day
         */
        val maxCounters: MaxCounter,
        /**
         * The dates where this specific integration has changed
         */
        val updateDates: Array<String>
    ) : HippoAction()

    /**
     * The state is updated with information from a bookmark
     */
    data class ApplyBookmark(
        val view: View,
        val bookmark: BookmarkInformation
    ) : HippoAction()
    object StartDownloadStatistics : HippoAction()
    data class DoneDownloadStatistics(
        val statisticsArrArr: Array<Array<Int>>
    ) : HippoAction()
    object StartDownloadHistory : HippoAction()
    data class DoneDownloadHistory(
        val historyMap: Map<String, Int>
    ) : HippoAction()
    data class ErrorDownloadIntegrations(val errorMessage: String) : HippoAction()

    /**
     * Called when a date is selected in hippo
     */
    data class DateSelected(val dateType: DateType, val selectedDate: String) : HippoAction()

    /**
     * Called when a plattform is selected in stat
     */
    data class StatTpSelected(val tpId: Int) : HippoAction()

    /**
     * Called when an item is selected
     */
    data class ItemIdSelected(
        val viewType: ItemType,
        val id: Int
    ) : HippoAction()
    data class ItemIdDeselected(val viewType: ItemType, val id: Int) : HippoAction()
    data class SetVMax(val type: ItemType, val size: Int) : HippoAction()
    data class ShowTimeGraph(val isShown: Boolean) : HippoAction()
    data class ShowConsumers(val isShown: Boolean) : HippoAction()
    data class ShowProduceras(val isShown: Boolean) : HippoAction()
    data class ShowLogicalAddresses(val isShown: Boolean) : HippoAction()
    data class ShowContracts(val isShown: Boolean) : HippoAction()
    data class ShowAllItemTypes(val isShown: Boolean) : HippoAction()
    data class LockShowAllItemTypes(val isShown: Boolean) : HippoAction()
    data class ShowTechnicalTerms(val isShown: Boolean) : HippoAction()
    data class SetPreselect(val preSelect: PreSelect?) : HippoAction()
}
