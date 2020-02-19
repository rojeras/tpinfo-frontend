package se.skoview.app

import pl.treksoft.kvision.Application
import pl.treksoft.kvision.pace.Pace
import pl.treksoft.kvision.panel.root
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.redux.createReduxStore
import pl.treksoft.kvision.require
import pl.treksoft.kvision.startApplication
import pl.treksoft.kvision.utils.perc
import se.skoview.data.*
import se.skoview.view.hippoTablePage
import se.skoview.view.setUrlFilter

// done: Fixa så det går att kopiera text utan att itemet väljs bort
// todo: Lite hjälptexter, troligen på egen sida
// done: Höjden på datumraden
// done: Kolumnerna ändrar fortfarande bredd
// done: Fixa val och fritextsökning av plattformChains
// done: Fixa till URL-hanteringen
// done: Fixa så att cursorn anpassar sig
// done: Stödtext efter item 100
// done: Snygga till ramarna och marginalerna
// done: Byta plats på logiska adresser och producenter
// todo: Gå igenom all kod, städa och refacotrera det viktigaste
// todo: Tag bort hand-pekaren när man pekar på ett valt item
// todo: Fixa till färgerna
// todo: Steg 1 Driftsätt K-hippo (rhippo)
// todo: Steg 2 Lös detta med att visa SE
// todo: Steg 3 Tag fram mock för hur integrationer ska presenteras där det kan finnas flera LA
// todo: Steg 4 Lös trädklättringen, kanske mha HSA-trädet
// todo: Publicera rhippo på hippokrates.se
// todo: Markera om ingen träff i sökning på nåt sätt

// Och efter produktionssättningen, i 1.1
// todo: Förbättre svarstiderna för fritextsökningen
// todo: Setting för att bestämma ordning på kolumnerna
// todo: Färger på rubrikerna
// todo: Frys rubrikraden


val store = createReduxStore(
    ::hippoReducer,
    getInitialState()
)

class App : Application() {
    init {
        require("css/hippo.css")
    }

    override fun start() {

        root("hippo") {

            vPanel {
                add(hippoTablePage)
            }.apply {
                width = 100.perc
            }
        }

        // A listener that sets the URL after each state change
        store.subscribe { state ->
            println("Store subscribe - will set URL")
            setUrlFilter(state)
        }

        Pace.init()
        loadBaseItems(store)
    }
}

fun main() {
    startApplication(::App)
}
