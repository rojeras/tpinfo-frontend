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

// todo: Fixa så det går att kopiera text utan att itemet väljs bort
// todo: Färger på rubrikerna
// todo: Lite hjälptexter
// todo: Show item id as tooltip
// todo: Tag bort v-datat från state. Detta kan tas fram dynamiskt när informationen presenteras
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
                add(hippoPage)
            }.apply {
                width = 100.perc
            }
        }

        store.subscribe { state ->
            stateChangeTrigger(state)
        }

        Pace.init()
        store.dispatch(HippoAction.ApplicationStarted)
    }

}

fun main() {
    startApplication(::App)
}
