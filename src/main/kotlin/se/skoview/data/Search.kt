package se.skoview.data

data class SearchIndex(val integrationArrs: ArrayList<Integration>) {
    data class IndexLine(
        val consumers: List<Int>,
        val producers: List<Int>,
        val domains: List<Int>,
        val contracts: List<Int>,
        val logialAddresses: List<Int>,
        val plattformChains: List<Int>
    )

    // One line per line in IntegrationsArrs
    val index = hashMapOf<String, IndexLine>()

    init {
        // Populate the index

    }

}