package se.skoview.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import pl.treksoft.kvision.redux.ReduxStore
import se.skoview.lib.getAsyncTpDb

fun loadBaseItems(store: ReduxStore<HippoState, HippoAction>) {
    console.log("Will now load BaseItems")
    store.dispatch(HippoAction.StartDownloadBaseItems)

    BaseDates.load({ areAllBaseItemsLoaded(store) })
    ServiceDomain.load({ areAllBaseItemsLoaded(store) })
    ServiceContract.load({ areAllBaseItemsLoaded(store) })
    ServiceComponent.load({ areAllBaseItemsLoaded(store) })
    LogicalAddress.load({ areAllBaseItemsLoaded(store) })
    Plattform.load({ areAllBaseItemsLoaded(store) })
    PlattformChain.load({ areAllBaseItemsLoaded(store) })
}

fun areAllBaseItemsLoaded(store: ReduxStore<HippoState, HippoAction>) {
    if (LogicalAddress.isLoaded &&
        PlattformChain.isLoaded &&
        Plattform.isLoaded &&
        ServiceComponent.isLoaded &&
        ServiceContract.isLoaded &&
        ServiceDomain.isLoaded &&
        BaseDates.isLoaded
    )
        store.dispatch { action, getState ->
            store.dispatch(HippoAction.DoneDownloadBaseItems)
            loadIntegrations(getState())
        }
}

abstract class BaseItem {
    abstract val description: String
    abstract val name: String
    abstract val searchField: String
    abstract val id: Int
}

object BaseDates {
    val integrationDates = mutableListOf<String>()
    val statisticsDates = mutableListOf<String>()
    var isLoaded = false

    fun load(callback: () -> Unit) {
        @Serializable
        data class BaseDatesJsonParseContent(val integrations: Array<String>, val statistics: Array<String>)

        data class BaseDatesJsonParse(val dates: BaseDatesJsonParseContent)

        val type = "dates"
        getAsyncTpDb(type) { response ->
            val items = JSON.parse<BaseDatesJsonParse>(response)
            for (integration in items.dates.integrations) {
                integrationDates.add(integration)
            }
            for (statistics in items.dates.statistics) {
                statisticsDates.add(statistics)
            }

            isLoaded = true
            callback()
        }
    }
}

@Serializable
data class ServiceComponent(
    override val id: Int = -1,
    val hsaId: String = "",
    override val description: String = "",
    val synonym: String? = null
) : BaseItem() {

    init {
        map[id] = this
    }

    override val name: String = hsaId
    //override val itemType = ItemType.COMPONENT
    override val searchField = "$name $description"

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

        fun load(callback: () -> Unit) {

            val type = "components"

            getAsyncTpDb(type) { response ->
                println("Size of response is: ${response.length}")
                val json = Json(JsonConfiguration.Stable)
                val serviceComponents: List<ServiceComponent> =
                    json.parse(ServiceComponent.serializer().list, response)

                isLoaded = true
                callback
            }
        }
    }
}

data class LogicalAddress constructor(
    override val id: Int,
    override val name: String,
    override val description: String
) : BaseItem() {

    init {
        map[id] = this
    }

    override val searchField = "$name $description"

    override fun toString(): String = "$name : $description"

    companion object {
        val map = hashMapOf<Int, LogicalAddress>()

        var isLoaded = false

        fun load(callback: () -> Unit) {
            @Serializable
            data class LogicalAddressJsonParse constructor(
                val id: Int,
                val logicalAddress: String,
                val description: String
            )

            val type = "logicalAddress"
            getAsyncTpDb(type) { response ->
                val items = JSON.parse<Array<LogicalAddressJsonParse>>(response)
                items.forEach { item ->
                    LogicalAddress(item.id, item.logicalAddress, item.description)
                }
                isLoaded = true
                callback()
            }
        }
    }
}

data class ServiceContract(
    override val id: Int,
    val serviceDomainId: Int,
    override val name: String,
    val namespace: String,
    val major: Int
) : BaseItem() {
    private val domain: ServiceDomain?

    init {
        map[id] = this
        domain = ServiceDomain.map[serviceDomainId]
    }

    override var searchField: String = namespace
    //override val itemType = ItemType.CONTRACT
    override val description = "$name v$major"

    override fun toString(): String = namespace

    companion object {
        val map = hashMapOf<Int, ServiceContract>()

        var isLoaded = false

        fun load(callback: () -> Unit) {
            @Serializable
            data class ServiceContractJsonParse(
                val id: Int,
                val serviceDomainId: Int,
                val name: String,
                val namespace: String,
                val major: Int
            )

            val type = "contracts"
            getAsyncTpDb(type) { response ->
                val items = JSON.parse<Array<ServiceContractJsonParse>>(response)
                items.forEach { item ->
                    ServiceContract(item.id, item.serviceDomainId, item.name, item.namespace, item.major)
                }
                isLoaded = true


                if (ServiceDomain.isLoaded) {
                    ServiceDomain.attachContractsToDomains()
                }
                callback()
            }
        }
    }
}

data class ServiceDomain(override val id: Int, override val name: String) : BaseItem() {
    //override val itemType = ItemType.DOMAIN
    var contracts: MutableSet<ServiceContract> = mutableSetOf()

    override val description = name

    init {
        // todo: Add logic to populate contracts
        map[id] = this
    }

    override val searchField: String = name

    override fun toString(): String = name

    companion object {
        val map = hashMapOf<Int, ServiceDomain>()
        var isLoaded = false

        fun load(callback: () -> Unit) {
            @Serializable
            data class ServiceDomainJsonParse(val id: Int, val domainName: String)

            val type = "domains"
            getAsyncTpDb(type) { response ->
                val items = JSON.parse<Array<ServiceDomainJsonParse>>(response)
                items.forEach { item ->
                    ServiceDomain(item.id, item.domainName)
                }
                isLoaded = true
                if (ServiceContract.isLoaded) {
                    attachContractsToDomains()
                }
                callback()
            }
        }

        fun attachContractsToDomains() {
            // Connect the contracts to its domain
            ServiceContract.map.forEach { (_, contract: ServiceContract) ->
                val domainId = contract.serviceDomainId
                val domain = map[domainId]
                if (domain != null) {
                    val contractList: MutableSet<ServiceContract> = domain.contracts
                    contractList.add(contract)
                }
            }
        }
    }
}

data class Plattform(override val id: Int, val platform: String, val environment: String, val snapshotTime: String) :
    BaseItem() {
    //override val itemType = ItemType.PLATTFORM
    override val name: String = "$platform-$environment"
    override val description = ""
    override val searchField: String = name
    override fun toString(): String = name

    init {
        map[id] = this
    }

    companion object {
        val map = hashMapOf<Int, Plattform>()
        var isLoaded = false

        fun load(callback: () -> Unit) {
            @Serializable
            data class PlattformJsonParse(
                val id: Int,
                val platform: String,
                val environment: String,
                val snapshotTime: String
            )

            val type = "plattforms"
            getAsyncTpDb(type) { response ->
                val items = JSON.parse<Array<PlattformJsonParse>>(response)
                items.forEach { item ->
                    Plattform(item.id, item.platform, item.environment, item.snapshotTime)
                }
                isLoaded = true
                callback()
            }
        }
    }

}

data class PlattformChain(val first: Int, val middle: Int?, val last: Int) : BaseItem() {

    private val firstPlattform = Plattform.map[first]
    //private val middlePlattform = Plattform.map[middle]
    private val lastPlattform = Plattform.map[last]

    override val id = calculateId(first, middle, last)
    override val name: String = calculateName()
    override val description = ""
    override val searchField: String = calculateName()

    init {
        //name = calculateName()
        map[id] = this
    }

    override fun toString(): String = firstPlattform!!.name + "->" + lastPlattform!!.name

    private fun calculateName(): String {
        val arrow = '\u2192'
        return if (firstPlattform == lastPlattform) {
            lastPlattform?.name ?: ""
        } else {
            firstPlattform?.name + " " + arrow + " " + lastPlattform?.name
        }
    }

    companion object {
        val map = hashMapOf<Int, PlattformChain>()
        var isLoaded = false

        fun load(callback: () -> Unit) {
            @Serializable
            data class PlattformChainJsonParse(val id: Int, val plattforms: Array<Int?>)

            val type = "plattformChains"
            getAsyncTpDb(type) { response ->
                val items = JSON.parse<Array<PlattformChainJsonParse>>(response)
                items.forEach { item ->
                    val f = item.plattforms[0] ?: 0
                    val l = item.plattforms[2] ?: 0
                    PlattformChain(f, item.plattforms[1], l)
                }
                isLoaded = true
                callback()
            }
        }

        // Calculte a plattformChainId based on ids of three separate plattforms
        fun calculateId(first: Int, middle: Int?, last: Int): Int {
            val saveM: Int = middle ?: 0
            return (first * 10000) + saveM * 100 + last
        }

        fun calculateSeparatePlattformIds(plattformChainId: Int): Triple<Int, Int, Int> {
            var id = plattformChainId

            val first = id / 10000
            id -= first
            val middle = id / 100
            id -= middle
            val last = id

            println("$plattformChainId converted to Triple<$first, $middle, $last")

            return Triple(first, middle, last)
        }
    }
}



