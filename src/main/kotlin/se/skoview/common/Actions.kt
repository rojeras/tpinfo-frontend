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

import pl.treksoft.kvision.redux.RAction
import se.skoview.stat.AdvancedViewPreSelect
import se.skoview.stat.SimpleViewPreSelect

/** According to: https://github.com/redux-utilities/flux-standard-action
 * An action object should have a type and contain:
 * - payload: data object | error object (if error == true) ?
 * - error: Boolean?
 * - meta: object?
 */

sealed class HippoAction : RAction {
    data class ApplicationStarted(val App: HippoApplication) : HippoAction()
    data class SetView(val view: View) : HippoAction()
    data class SetDownloadBaseDatesStatus(val status: AsyncActionStatus) : HippoAction()
    object StartDownloadBaseItems : HippoAction()
    object DoneDownloadBaseItems : HippoAction()
    data class ErrorDownloadBaseItems(val errorMessage: String) : HippoAction()
    object StartDownloadIntegrations : HippoAction()
    data class DoneDownloadIntegrations(
        val integrationArrs: List<Integration>,
        val maxCounters: MaxCounter,
        val updateDates: Array<String>
    ) : HippoAction()
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
    data class DateSelected(val dateType: DateType, val selectedDate: String) : HippoAction()
    data class StatTpSelected(val tpId: Int) : HippoAction()
    data class ItemIdSelected(
        val viewType: ItemType,
        val id: Int
    ) : HippoAction()
    data class ItemIdDeselected(val viewType: ItemType, val id: Int) : HippoAction()
    data class SetVMax(val type: ItemType, val size: Int) : HippoAction()
    data class ShowTimeGraph(val isShown: Boolean) : HippoAction()
    data class ShowTechnicalTerms(val isShown: Boolean) : HippoAction()
    // data class SetViewMode(val viewMode: ViewMode) : HippoAction()
    data class SetSimpleViewPreselect(val preSelect: SimpleViewPreSelect) : HippoAction()
    data class SetAdvancedViewPreselect(val preSelect: AdvancedViewPreSelect) : HippoAction()
}
