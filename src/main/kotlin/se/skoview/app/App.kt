package se.skoview.app

import pl.treksoft.kvision.Application
import pl.treksoft.kvision.i18n.DefaultI18nManager
import pl.treksoft.kvision.i18n.I18n
import pl.treksoft.kvision.pace.Pace
import pl.treksoft.kvision.panel.root
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.require
import pl.treksoft.kvision.startApplication
import pl.treksoft.kvision.utils.perc
import se.skoview.data.*
import se.skoview.view.hippoTablePage

// todo: Fixa så det går att kopiera text utan att itemet väljs bort
// todo: Färger på rubrikerna
// todo: Lite hjälptexter
// todo: Show item id as tooltip
// todo: Byta plats på logiska adresser och producenter
// todo: Stöd för backpil (dvs backa)
// todo: Byt ut den gröna färgen i headern
// todo: Steg 1 Driftsätt K-hippo (rhippo)
// todo: Steg 2 Lös detta med att visa SE
// todo: Steg 3 Tag fram mock för hur integrationer ska presenteras där det kan finnas flera LA
// todo: Steg 4 Lös trädklättringen, kanske mha HSA-trädet
// todo: Publicera rhippo på hippokrates.se


/*
Kommentarer från ML:
rhippo
    Rörigt med scrollbars
    Sidnumren försvinner när skärmen minskas ner
    Lägg in texterna: Sök tjänsteplattformar etc
    Skippa sidescroll
        Kanske bara klippa

 */

class App : Application() {
    init {
        require("css/kvapp.css")
    }

    override fun start() {
        I18n.manager =
            DefaultI18nManager(
                mapOf(
                    "pl" to require("i18n/messages-pl.json"),
                    "en" to require("i18n/messages-en.json")
                )
            )

        root("kvapp") {

            vPanel {
                add(hippoTablePage)
            }.apply {
                width = 100.perc
            }
        }

        /*
        store.subscribe { state ->
            println("Store subscribe before call to stateChangeTrigger")
            stateChangeTrigger(state)
            println("Store subscribe after call to stateChangeTrigger")
        }
         */

        Pace.init()
        loadBaseItems(store)
        //store.dispatch(HippoAction.ApplicationStarted)
    }

}

fun main() {
    startApplication(::App)
}
