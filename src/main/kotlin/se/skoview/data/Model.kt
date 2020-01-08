package se.skoview.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import pl.treksoft.kvision.redux.ActionCreator
import pl.treksoft.kvision.redux.RAction
import se.skoview.data.ServiceComponent.Companion.isLoaded
import se.skoview.lib.getAsyncTpDb

@Serializable
data class HippoState(
    val downloading: Boolean,
    val errorMessage: String?,
    val components: List<ServiceComponent> // Will probably not be stored in the state
)

sealed class HippoAction : RAction {
    object StartDownload : HippoAction()
    object DownloadOk : HippoAction()
    data class SetServiceComponentList(val components: List<ServiceComponent>) : HippoAction()
    data class DownloadError(val errorMessage: String) : HippoAction()
}

//fun hippoReducer(state: HippoState, action: HippoAction): HippoState = when (action) {
fun hippoReducer(state: HippoState, action: HippoAction): HippoState {
    println("----> In hippoReducer, action=${action::class}")
    return when (action) {
        is HippoAction.StartDownload -> state.copy(downloading = true)
        is HippoAction.DownloadOk -> {
            state.copy(downloading = false)
        }
        is HippoAction.SetServiceComponentList -> state
        is HippoAction.DownloadError -> state.copy(downloading = false, errorMessage = action.errorMessage)
    }
}

// ---------------------------------------------------------------------------------------------------------------------
/*
@Serializable
data class ServiceComponent(
    val id: Int,
    val hsaId: String,
    val description: String = "",
    val synonym: String? = null
) {
    init {
        map[id] = this
    }

    val name: String = hsaId
    //override val itemType = ItemType.COMPONENT
    var searchField = "$name $description"

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is ServiceComponent) return false
        return id == other.id
    }

    override fun toString(): String {
        return "ServiceComponent(id=$id, name=$name, description=$description)"
    }

    override fun hashCode(): Int {
        return id
    }

    companion object {
        val map = hashMapOf<Int, ServiceComponent>()

        var isLoaded = false
    }
}
 */
fun downloadServiceComponents(): ActionCreator<dynamic, HippoState> {
    return { dispatch, _ ->
        val baseUrl = "https://qa.integrationer.tjansteplattform.se/tpdb/tpdbapi.php/api/v1/components"
        //val baseUrl = "https://pokeapi.co/api/v2/pokemon/"
        println("After url")
        dispatch(HippoAction.StartDownload)
        println("After dispatch")

        getAsyncTpDb("components") { response ->
            println("Size of response is: ${response.length}")
            val json = Json(JsonConfiguration.Stable)
            val serviceComponents: List<ServiceComponent> =
                json.parse(ServiceComponent.serializer().list, response)

            console.log(serviceComponents)

            isLoaded = true
            dispatch(HippoAction.DownloadOk)
            dispatch(HippoAction.SetServiceComponentList(serviceComponents))
        }
    }
}
