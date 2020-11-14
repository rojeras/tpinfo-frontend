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
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import pl.treksoft.kvision.redux.ReduxStore
import pl.treksoft.kvision.rest.HttpMethod
import pl.treksoft.kvision.rest.RestClient
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.js.Promise

fun loadBaseItems(store: ReduxStore<HippoState, HippoAction>) { // : Deferred<Unit> {
    println("Will now load BaseItems")
    // We must ensure the dates are loaded synchronisly - integration and statistics fetch are dependent

    store.dispatch(HippoAction.StartDownloadBaseItems)
    GlobalScope.async {

        // val baseDatesJob = GlobalScope.launch {
        val p1 = loadBaseItem("dates", Dates.serializer())
        // }

        // val domainsJob = GlobalScope.launch {
        val p2 = loadBaseItem("domains", ListSerializer(ServiceDomain.serializer()))
        // }

        // val contractsJob = GlobalScope.launch {
        val p3 = loadBaseItem("contracts", ListSerializer(ServiceContract.serializer()))
        // }

        // val componentJob = GlobalScope.launch {
        val p4 = loadBaseItem("components", ListSerializer(ServiceComponent.serializer()))
        // }
/*
        // val laJob = GlobalScope.launch {
        loadBaseItem("logicalAddress", ListSerializer(LogicalAddress.serializer()))
        // }

        // val plattformJob = GlobalScope.launch {
        loadBaseItem("plattforms", ListSerializer(Plattform.serializer()))
        // }

        // val plattformChainJob = GlobalScope.launch {
        loadBaseItem("plattformChains", ListSerializer(PlattformChainJson.serializer()))
        // }

        // val statPlattformJob = GlobalScope.launch {
        loadBaseItem("statPlattforms", ListSerializer(StatisticsPlattform.serializer()))
        // }
*/
        p1.await()
        p2.await()
        p3.await()
        p4.await()

        /*
        joinAll(
            baseDatesJob,
            domainsJob,
            contractsJob,
            componentJob,
            laJob,
            plattformJob,
            plattformChainJob,
            statPlattformJob
        )
         */
        println("After join")
        ServiceDomain.attachContractsToDomains()
        println("Will now dispatch DoneDownloadBaseItems")
        store.dispatch(HippoAction.DoneDownloadBaseItems)
    }
}

suspend fun <T : Any> loadBaseItem(type: String, deserializer: DeserializationStrategy<T>): Promise<T> {
    val restClient = RestClient()
    val url = "${tpdbBaseUrl()}$type"
    println("*** In load $type")
    println(url)

    val answerPromise =
        restClient.remoteCall(
            url = url,
            method = HttpMethod.GET,
            deserializer = deserializer, // ListSerializer(ServiceComponent.serializer()),
            contentType = ""
        )

    // answerPromise.await()
    println("*** Leaving load $type")
    return answerPromise
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
        var integrationDates = listOf<String>()
        var statisticsDates = listOf<String>()
    }
}

@Serializable
data class ServiceComponent(
    override val id: Int = -1,
    override val hsaId: String = "",
    override val description: String = "",
    override val synonym: String? = null
) : BaseItem() {

    init {
        map[id] = this
        // if (id > maxId) maxId = id
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
        // var maxId = 0
    }
}

@Serializable
data class LogicalAddress constructor(
    override val id: Int,
    override val description: String,
    override val synonym: String? = null,
    val logicalAddress: String
) : BaseItem() {
    override val name = logicalAddress
    var colorValue: Int = (0..(256 * 256 * 256) - 1).random()

    init {
        map[id] = this

        //  if (id > LogicalAddress.maxId) LogicalAddress.maxId = id
    }

    override val searchField = "$name $description"

    override fun toString(): String = "$name : $description"

    companion object {
        val map = hashMapOf<Int, LogicalAddress>()
        // var maxId = 0
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

    init {
        map[id] = this
        // if (id > ServiceContract.maxId) ServiceContract.maxId = id
    }

    override var searchField: String = namespace

    override val description = "$name v$major"

    override fun toString(): String = namespace

    companion object {
        val map = hashMapOf<Int, ServiceContract>()

        // var maxId = 0
    }
}

@Serializable
data class ServiceDomain(
    override val id: Int,
    val domainName: String,
    override val synonym: String? = null
) : BaseItem() {
    override val name = domainName

    var contracts: MutableSet<ServiceContract> = mutableSetOf()

    override val description = name

    init {
        // todo: Add logic to populate contracts
        map[id] = this
        // if (id > ServiceDomain.maxId) ServiceDomain.maxId = id
    }

    override val searchField: String = name

    override fun toString(): String = name

    companion object {
        val map = hashMapOf<Int, ServiceDomain>()
        // var maxId = 0

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
        // if (id > Plattform.maxId) Plattform.maxId = id
    }

    companion object {
        val map = hashMapOf<Int, Plattform>()
        // var maxId = 0
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

    private val lastPlattform = Plattform.map[last]

    override val id = calculateId(first, middle, last)
    override val name: String = calculateName()
    override val description = ""
    override val searchField: String = calculateName()

    init {
        map[id] = this

        //  if (id > PlattformChain.maxId) PlattformChain.maxId = id
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
        // var maxId = 0

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
    override val name: String = "$platform-$environment"
    override val description = ""
    override val searchField: String = name
    override fun toString(): String = name

    init {
        mapp[id] = this
    }

    companion object {
        val mapp = hashMapOf<Int, StatisticsPlattform>()
    }
}
