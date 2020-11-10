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

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import pl.treksoft.kvision.redux.ReduxStore
import pl.treksoft.kvision.rest.HttpMethod
import pl.treksoft.kvision.rest.RestClient
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

suspend fun loadBaseItems(store: ReduxStore<HippoState, HippoAction>) {
    println("Will now load BaseItems")
    store.dispatch(HippoAction.StartDownloadBaseItems)

    // BaseDates.load { areAllBaseItemsLoaded(store) }
    val baseDatesJob = GlobalScope.launch {
        loadBaseItem("dates", Dates.serializer())
        BaseDates.isLoaded = true
        // areAllBaseItemsLoaded(store)
    }

    // ServiceDomain.load { areAllBaseItemsLoaded(store) }
    val domainsJob = GlobalScope.launch {
        loadBaseItem("domains", ListSerializer(ServiceDomain.serializer()))
        ServiceDomain.isLoaded = true
        // areAllBaseItemsLoaded(store)
    }

    // ServiceContract.load { areAllBaseItemsLoaded(store) }
    val contractsJob = GlobalScope.launch {
        loadBaseItem("contracts", ListSerializer(ServiceContract.serializer()))
        ServiceContract.isLoaded = true
        // areAllBaseItemsLoaded(store)
    }

    // ServiceComponent.load { areAllBaseItemsLoaded(store) }
    val componentJob = GlobalScope.launch {
        loadBaseItem("components", ListSerializer(ServiceComponent.serializer()))
        ServiceComponent.isLoaded = true
        // areAllBaseItemsLoaded(store)
    }

    // LogicalAddress.load { areAllBaseItemsLoaded(store) }
    val laJob = GlobalScope.launch {
        loadBaseItem("logicalAddress", ListSerializer(LogicalAddress.serializer()))
        LogicalAddress.isLoaded = true
        // areAllBaseItemsLoaded(store)
    }


    // Plattform.load { areAllBaseItemsLoaded(store) }
    val plattformJob = GlobalScope.launch {
        loadBaseItem("plattforms", ListSerializer(Plattform.serializer()))
        Plattform.isLoaded = true
        // areAllBaseItemsLoaded(store)
    }

    // PlattformChain.load { areAllBaseItemsLoaded(store) }
    val plattformChainJob = GlobalScope.launch {
        loadBaseItem("plattformChains", ListSerializer(PlattformChainJson.serializer()))
        PlattformChain.isLoaded = true
        // areAllBaseItemsLoaded(store)
    }

    // StatisticsPlattform.load { areAllBaseItemsLoaded(store) }
    val statPlattformJob = GlobalScope.launch {
        loadBaseItem("statPlattforms", ListSerializer(StatisticsPlattform.serializer()))
        StatisticsPlattform.isLoaded = true
        // areAllBaseItemsLoaded(store)
    }

    println("Before Joinall")
    joinAll(baseDatesJob, domainsJob, contractsJob, componentJob, laJob, plattformJob, plattformChainJob, statPlattformJob)
    println("After Joinall")

    ServiceDomain.attachContractsToDomains()

    store.dispatch(HippoAction.DoneDownloadBaseItems)
}

suspend fun <T : Any> loadBaseItem(type: String, deserializer: DeserializationStrategy<T>) {
    val restClient = RestClient()
    val url = "${tpdbBaseUrl()}$type"
    println(url)
    println("*** In load $type")

    val componentList =
        restClient.remoteCall(
            url = url,
            method = HttpMethod.GET,
            deserializer = deserializer, // ListSerializer(ServiceComponent.serializer()),
            contentType = ""
        )

    println("*** In load $type - after restClient() call")
    console.log(componentList)
    val sss = componentList.await()
    println("*** In load $type - after await()")
    console.log(sss)
    // callback()
}

fun areAllBaseItemsLoaded(store: ReduxStore<HippoState, HippoAction>) {
    if (LogicalAddress.isLoaded &&
        PlattformChain.isLoaded &&
        Plattform.isLoaded &&
        ServiceComponent.isLoaded &&
        ServiceContract.isLoaded &&
        ServiceDomain.isLoaded &&
        // BaseDates.isLoaded &&
        StatisticsPlattform.isLoaded
    )
        store.dispatch(HippoAction.DoneDownloadBaseItems)
}

abstract class BaseItem {
    abstract val description: String
    abstract val name: String
    abstract val searchField: String
    abstract val id: Int
    open val hsaId = ""
    abstract val synonym: String?
}

@Serializable
data class Dates(
    val dates: BaseDates
)

@Serializable
data class BaseDates(
    val integrations: List<String>,
    val statistics: List<String>
) {
    init {
        integrationDates = integrations
        statisticsDates = statistics
    }

    companion object {
        var isLoaded = false
        var integrationDates = listOf<String>()
        var statisticsDates = listOf<String>()
    }
}

/*
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
 */

@Serializable
data class ServiceComponent(
    override val id: Int = -1,
    override val hsaId: String = "",
    override val description: String = "",
    override val synonym: String? = null
) : BaseItem() {

    var colorValue: Int = (0..(256 * 256 * 256) - 1).random()

    init {
        map[id] = this
        if (id > maxId) maxId = id
    }

    override val name: String = hsaId

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
        var maxId = 0
        var isLoaded = false
/*
        suspend fun load(callback: () -> Unit) {
            val type = "components"
            val restClient = RestClient()
            val url = "${tpdbBaseUrl()}/$type"
            println(url)
            println("*** In load $type")

            val componentList =
                restClient.remoteCall(
                    url = url,
                    method = HttpMethod.GET,
                    deserializer = ListSerializer(ServiceComponent.serializer()),
                    contentType = ""
                )

            println("*** In load $type - after restClient() call")
            console.log(componentList)
            val sss = componentList.await()
            println("*** In load $type - after await()")
            console.log(sss)
            isLoaded = true
            callback()
        }
 */

        /*
        fun abc() {
            @Serializable
            data class Repository(val id: Int, val full_name: String?, val description: String?, val fork: Boolean)

            val restClient = RestClient()
            val items: Promise<List<Repository>> = restClient.remoteCall(
                "https://api.github.com/search/repositories",
                obj { q = "kvision" },
                deserializer = Repository.serializer().list
            ) {
                it.items
            }
        }

        fun load2(callback: () -> Unit) {

            getAsyncTpDb("components") { response ->
                println("Size of response for ServiceComponents are: ${response.length}")
                println(response.substring(0, 200))

                val json = Json {}
                json.decodeFromString(ListSerializer(ServiceComponent.serializer()), response)

                isLoaded = true
                callback()
            }
        }
        */
    }
}

@Serializable
data class LogicalAddress constructor(
    override val id: Int,
    // override val name: String? = null,
    override val description: String,
    override val synonym: String? = null,
    val logicalAddress: String
) : BaseItem() {
    override val name = logicalAddress
    var colorValue: Int = (0..(256 * 256 * 256) - 1).random()

    init {
        map[id] = this

        if (id > LogicalAddress.maxId) LogicalAddress.maxId = id
    }

    override val searchField = "$name $description"

    override fun toString(): String = "$name : $description"

    companion object {
        val map = hashMapOf<Int, LogicalAddress>()
        var maxId = 0
        var isLoaded = false

        /*
        fun load(callback: () -> Unit) {
            @Serializable
            data class LogicalAddressJsonParse constructor(
                val id: Int,
                val logicalAddress: String,
                val description: String,
                val synonym: String
            )

            val type = "logicalAddress"
            getAsyncTpDb(type) { response ->
                val items = JSON.parse<Array<LogicalAddressJsonParse>>(response)
                items.forEach { item ->
                    LogicalAddress(item.id, item.logicalAddress, item.description, item.synonym)
                }
                isLoaded = true
                callback()
            }
        }
         */
    }
}

@Serializable
data class ServiceContract(
    override val id: Int,
    val serviceDomainId: Int,
    override val name: String,
    val namespace: String,
    val major: Int,
    override val synonym: String? = null
) : BaseItem() {
    // private val domain: ServiceDomain?

    init {
        map[id] = this
        // domain = ServiceDomain.map[serviceDomainId]

        if (id > ServiceContract.maxId) ServiceContract.maxId = id
    }

    override var searchField: String = namespace

    // override val itemType = ItemType.CONTRACT
    override val description = "$name v$major"

    override fun toString(): String = namespace

    companion object {
        val map = hashMapOf<Int, ServiceContract>()

        var maxId = 0

        var isLoaded = false
/*
        fun load(callback: () -> Unit) {
            @Serializable
            data class ServiceContractJsonParse(
                val id: Int,
                val serviceDomainId: Int,
                val name: String,
                val namespace: String,
                val major: Int,
                val synonym: String
            )

            val type = "contracts"
            getAsyncTpDb(type) { response ->
                val items = JSON.parse<Array<ServiceContractJsonParse>>(response)
                items.forEach { item ->
                    ServiceContract(item.id, item.serviceDomainId, item.name, item.namespace, item.major, item.synonym)
                }
                isLoaded = true

                if (ServiceDomain.isLoaded) {
                    ServiceDomain.attachContractsToDomains()
                }
                callback()
            }
        }
        */
    }
}

@Serializable
data class ServiceDomain(
    override val id: Int,
    val domainName: String,
    override val synonym: String? = null
) : BaseItem() {
    override val name = domainName

    // override val itemType = ItemType.DOMAIN
    var contracts: MutableSet<ServiceContract> = mutableSetOf()

    override val description = name

    init {
        // todo: Add logic to populate contracts
        map[id] = this

        if (id > ServiceDomain.maxId) ServiceDomain.maxId = id
    }

    override val searchField: String = name

    override fun toString(): String = name

    companion object {
        val map = hashMapOf<Int, ServiceDomain>()
        var maxId = 0
        var isLoaded = false

        /*
        fun load(callback: () -> Unit) {
            @Serializable
            data class ServiceDomainJsonParse(val id: Int, val domainName: String, val synonym: String)

            val type = "domains"
            getAsyncTpDb(type) { response ->
                val items = JSON.parse<Array<ServiceDomainJsonParse>>(response)
                items.forEach { item ->
                    ServiceDomain(item.id, item.domainName, item.synonym)
                }
                isLoaded = true
                if (ServiceContract.isLoaded) {
                    attachContractsToDomains()
                }
                callback()
            }
        }
        */

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

@Serializable
data class Plattform(
    override val id: Int,
    val platform: String,
    val environment: String,
    val snapshotTime: String,
    override val synonym: String? = null
) :
    BaseItem() {
    // override val itemType = ItemType.PLATTFORM
    override val name: String = "$platform-$environment"
    override val description = ""
    override val searchField: String = name
    override fun toString(): String = name

    init {
        map[id] = this
        if (id > Plattform.maxId) Plattform.maxId = id
    }

    companion object {
        val map = hashMapOf<Int, Plattform>()
        var maxId = 0
        var isLoaded = false

        /*
        fun load(callback: () -> Unit) {
            @Serializable
            data class PlattformJsonParse(
                val id: Int,
                val platform: String,
                val environment: String,
                val snapshotTime: String,
                val synonym: String
            )

            val type = "plattforms"
            getAsyncTpDb(type) { response ->
                val items = JSON.parse<Array<PlattformJsonParse>>(response)
                items.forEach { item ->
                    Plattform(item.id, item.platform, item.environment, item.snapshotTime, item.synonym)
                }
                isLoaded = true
                callback()
            }
        }
        */
    }
}

@Serializable
data class PlattformChainJson(
    val id: Int,
    val plattforms: Array<Int?>
) {
    init {
        val f = plattforms[0] ?: 0
        val l = plattforms[2] ?: 0
        PlattformChain(f, plattforms[1], l)
    }
}

data class PlattformChain(
    val first: Int,
    val middle: Int?,
    val last: Int,
    override val synonym: String? = null
) : BaseItem() {

    var colorValue: Int = (0..(256 * 256 * 256) - 1).random()
    private val firstPlattform = Plattform.map[first]

    // private val middlePlattform = Plattform.map[middle]
    private val lastPlattform = Plattform.map[last]

    override val id = calculateId(first, middle, last)
    override val name: String = calculateName()
    override val description = ""
    override val searchField: String = calculateName()

    init {
        // name = calculateName()
        map[id] = this

        if (id > PlattformChain.maxId) PlattformChain.maxId = id
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
        var maxId = 0
        var isLoaded = false

        /*
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
        */
        // Calculte a plattformChainId based on ids of three separate plattforms
        fun calculateId(first: Int, middle: Int?, last: Int): Int {
            val saveM: Int = middle ?: 0
            return (first * 10000) + saveM * 100 + last
        }
    }
}

// List of plattforms containing statistics information
@Serializable
data class StatisticsPlattform(
    override val id: Int,
    val platform: String,
    val environment: String,
    override val synonym: String? = null
) : BaseItem() {
    // var colorValue: Int = (0..(256*256*256)-1).random()
    override val name: String = "$platform-$environment"
    override val description = ""
    override val searchField: String = name
    override fun toString(): String = name

    init {
        mapp[id] = this

        // if (id > StatisticsPlattform.maxId) StatisticsPlattform.maxId = id
    }

    companion object {
        val mapp = hashMapOf<Int, StatisticsPlattform>()

        // var maxId = 0
        var isLoaded = false

        /*
        fun load(callback: () -> Unit) {
            @Serializable
            data class StatisticsPlattformJsonParse(
                val id: Int,
                val platform: String,
                val environment: String,
                // val snapshotTime: String,
                // val synonym: String
            )

            val type = "statPlattforms"
            getAsyncTpDb(type) { response ->
                val items = JSON.parse<Array<StatisticsPlattformJsonParse>>(response)
                items.forEach { item ->
                    StatisticsPlattform(item.id, item.platform, item.environment/*, item.synonym*/)
                }
                isLoaded = true
                callback()
            }
        }
         */
    }
}
