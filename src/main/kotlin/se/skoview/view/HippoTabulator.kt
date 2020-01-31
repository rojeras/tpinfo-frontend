package se.skoview.view

import pl.treksoft.kvision.core.Background
import pl.treksoft.kvision.core.Col
import pl.treksoft.kvision.core.Color
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.table.TableType
import pl.treksoft.kvision.table.cell
import pl.treksoft.kvision.table.row
import pl.treksoft.kvision.table.table
import pl.treksoft.kvision.tabulator.*
import pl.treksoft.kvision.utils.perc
import se.skoview.data.HippoAction
import se.skoview.data.isItemFiltered
import se.skoview.data.loadIntegrations
import se.skoview.data.store

class HippoTabulatorPage(
    val consumerHeading: String,
    val contractHeading: String,
    val plattformHeading: String,
    val logicalAddressHeading: String,
    val producerHeading: String,
    val viewConsumerLst: MutableList<ViewInformation>,
    val viewContractLst: MutableList<ViewInformation>,
    val viewPlattformList: MutableList<ViewInformation>,
    val viewLogicalAddressList: MutableList<ViewInformation>,
    val viewProducerLst: MutableList<ViewInformation>
): SimplePanel() {
    init {
        table(
            listOf(
                "",
                "",
                "",
                "",
                ""
            ),
            setOf(TableType.SMALL, TableType.BORDERED),
            classes = setOf("table-layout:fixed")
        )
        {
            row(classes = setOf("table-layout:fixed")) {
                // Service consumers
                cell(classes = setOf("table-layout:fixed")) {
                    add(HippoTabulator(consumerHeading, viewConsumerLst))
                }.apply {
                    width = 20.perc
                }
                // Service contracts
                cell {
                    add(HippoTabulator(contractHeading, viewContractLst))
                }.apply {
                    width = 20.perc
                }
                // Plattforms
                cell {
                    add(HippoTabulator(plattformHeading, viewPlattformList))
                }.apply {
                    width = 15.perc
                }
                // Logical addresses
                cell {
                    add(HippoTabulator(logicalAddressHeading, viewLogicalAddressList))
                }.apply {
                    width = 20.perc
                }
                // Service producers
                cell {
                    add(HippoTabulator(producerHeading, viewProducerLst))
                }.apply {
                    width = 20.perc
                }
            }.apply {
                color = Color(Col.BLACK)
                width = 100.perc
            }
        }.apply {
            color = Color(0x009090)
            width = 100.perc
        }
    }
}

class HippoTabulator(
    columnHeader: String,
    itemList: List<ViewInformation>
) : SimplePanel() {
    init {
        tabulator(
            itemList,
            options = TabulatorOptions(
                columns = listOf(
                    ColumnDefinition(
                        columnHeader, "showData",

                        formatterComponentFunction = { _, _, item ->
                            //console.log("In ColumnDef: ${item.name}")
                            Div(rich = true) {
                                if (store.getState().isItemFiltered(item.type, item.baseItem.id)) {
                                    background = Background(Col.LIGHTSTEELBLUE)
                                }
                                +item.showData
                            }
                            //Div() { +item.hsaId }
                        },
                        headerFilter = Editor.INPUT
                    )
                ),
                layout = Layout.FITCOLUMNS,
                layoutColumnsOnNewData = false,
                pagination = PaginationMode.LOCAL,
                height = "80vh",
                paginationSize = 100,
                selectable = true,
                rowSelected = { row ->
                    console.log(row)
                    val viewItem = row.getData() as ViewInformation
                    val viewType = viewItem.type
                    val baseItem = viewItem.baseItem
                    store.dispatch { dispatch, getState ->
                        store.dispatch(HippoAction.ItemSelected(viewType, baseItem))
                        println("Time to download the integrations since an item has been selected/deselected")
                        console.log(getState())
                        loadIntegrations(getState())
                    }
                    println("Item clicked: $viewType")
                    console.log(baseItem)
                }
            )
        )
        {
            //height = 430.px
            width = 90.perc
        }
    }
}