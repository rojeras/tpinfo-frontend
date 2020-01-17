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
import se.skoview.view.hippoPage

// todo: Indikering när nedladdning pågår
// todo: Hantering av URL-er
// todo: Kolumnerna rör sig lite i sidled ibland
// todo: Färger på rubrikerna
// todo: Översta raden ska bort
// todo: Bredden på datumväljarenLite hjälptexter
// todo: Döp om och få bort "template"
// todo: Show item id as tooltip

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
                add(hippoPage)
            }.apply {
                width = 100.perc
            }
        }

        //store.dispatch(downloadServiceComponents())
        Pace.init()
        loadBaseItems(store)

    }

}

fun main() {
    startApplication(::App)
}
