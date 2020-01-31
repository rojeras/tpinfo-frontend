package se.skoview.data

import pl.treksoft.kvision.redux.RAction

/** According to: https://github.com/redux-utilities/flux-standard-action
 * An action object should have a type and contain:
 * - payload: data object | error object (if error == true) ?
 * - error: Boolean?
 * - meta: object?
 */

sealed class HippoAction : RAction {
    object ApplicationStarted : HippoAction()
    object StartDownloadBaseItems : HippoAction()
    object DoneDownloadBaseItems : HippoAction()
    data class ErrorDownloadBaseItems(val errorMessage: String) : HippoAction()
    object StartDownloadIntegrations : HippoAction()
    data class DoneDownloadIntegrations(
        val integrationArrs: List<Integration>,
        val maxCounters: MaxCounter,
        val updateDates: List<String>
    ) : HippoAction()
    data class ErrorDownloadIntegrations(val errorMessage: String) : HippoAction()
    data class DateSelected(val selectedDate: String) : HippoAction()
    data class ItemSelected(
        val viewType: ItemType,
        val baseItem: BaseItem
    ) : HippoAction()

    data class FilterConsumers(val consumerFilter: String) : HippoAction()
}
