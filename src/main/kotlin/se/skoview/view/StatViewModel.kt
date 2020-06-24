package se.skoview.view

import pl.treksoft.kvision.core.Color
import pl.treksoft.kvision.data.BaseDataComponent
import se.skoview.data.*
import se.skoview.lib.getColorForObject
// Dmmy change

class SInfoRecord(
    val itemType: ItemType,
    val itemId: Int,
    val description: String,
    val calls: Int
    //val responseTime: Int
) {
    val color: Color = getColorForObject(description)

    init {
        // A random color is assigned to each object (based on hash value of description field)
    }
}

class SInfoList(private val itemType: ItemType) {

    val recordList = mutableListOf<SInfoRecord>()

    fun callList(): List<Int> {
        return recordList.map { it.calls }
    }

    fun colorList(): List<Color> {
        return recordList.map { it.color }
    }

    fun descList(): List<String> {
        return recordList.map { it.description }
    }

    fun populate(ackMap: Map<Int, Int>) {
        // Will go via a temp collection
        val ackMapTmp = ackMap.toList().sortedBy { (_, value) -> value }.reversed().toMap()
        val callsTmp = mutableListOf<SInfoRecord>()

        var item: BaseItem?
        var desc: String
        for (entry in ackMapTmp) {
            when (this.itemType) {
                ItemType.CONSUMER -> {
                    item = ServiceComponent.map[entry.key]
                    desc = "${item!!.description} (${item.hsaId})"
                }
                ItemType.PRODUCER -> {
                    item = ServiceComponent.map[entry.key]
                    desc = "${item!!.description} (${item.hsaId})"
                }
                ItemType.LOGICAL_ADDRESS -> {
                    item = LogicalAddress.map[entry.key]
                    desc = "${item!!.description} (${item.name})"
                }
                ItemType.CONTRACT -> {
                    item = ServiceContract.map[entry.key]
                    desc = item!!.description
                }
                else -> error("Unknown itemType in populate()!")
            }
            //println("${item!!.description} has calls: ${ackMapTmp[entry.key]}")

            callsTmp.add(
                SInfoRecord(
                    this.itemType,
                    item.id,
                    desc,
                    ackMapTmp[entry.key] ?: error("ackMapTmp is null")
                    //,0
                )
            )
        }

        // Sort the recordList in reverse order based on number of calls
        callsTmp.sortBy { it.calls }
        callsTmp.reverse()

        this.recordList.clear()
        this.recordList.addAll(callsTmp)
    }
}

/**
 * The singleton for the data displayed in the view
 * It is updated through the view() method with an statisticsInfo object as parameter.
 */

object SInfo : BaseDataComponent() {
    var consumerSInfoList = SInfoList(ItemType.CONSUMER)
    var producerSInfoList = SInfoList(ItemType.PRODUCER)
    var logicalAddressSInfoList = SInfoList(ItemType.LOGICAL_ADDRESS)
    var contractSInfoList = SInfoList(ItemType.CONTRACT)

    fun createStatViewData(state: HippoState) {
        println("In the SInfo.createStatViewData()")
        consumerSInfoList.populate(state.callsConsumer)
        producerSInfoList.populate(state.callsProducer)
        logicalAddressSInfoList.populate(state.callsLogicalAddress)
        contractSInfoList.populate(state.callsContract)

    }
}