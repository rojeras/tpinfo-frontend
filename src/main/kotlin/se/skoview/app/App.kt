package se.skoview.app

import pl.treksoft.kvision.Application
import pl.treksoft.kvision.core.Container
import pl.treksoft.kvision.core.TextAlign
import pl.treksoft.kvision.html.div
import pl.treksoft.kvision.i18n.DefaultI18nManager
import pl.treksoft.kvision.i18n.I18n
import pl.treksoft.kvision.i18n.I18n.tr
import pl.treksoft.kvision.panel.FlexAlignItems
import pl.treksoft.kvision.panel.root
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.redux.ReduxStore
import pl.treksoft.kvision.redux.createReduxStore
import pl.treksoft.kvision.require
import pl.treksoft.kvision.startApplication
import pl.treksoft.kvision.state.stateBinding
import pl.treksoft.kvision.utils.auto
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px
import se.skoview.data.*

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

            add(hippoPage)
        }
        //store.dispatch(downloadServiceComponents())
        loadBaseItems(store)
    }

}

fun main() {
    startApplication(::App)
}
