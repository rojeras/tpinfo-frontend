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
            vPanel(alignItems = FlexAlignItems.STRETCH) {
                width = 100.perc
                //searchField()
                div { +"Message: " }
                vPanel(alignItems = FlexAlignItems.STRETCH) {
                    maxWidth = 1200.px
                    textAlign = TextAlign.CENTER
                    marginLeft = auto
                    marginRight = auto
                }.stateBinding(store) { state ->
                    informationText(state)
                    if (!state.downloadingBaseItems && state.errorMessage == null) {
                        //pokemonGrid(state)
                        //pagination(state)
                    }
                }
            }
        }
        //store.dispatch(downloadServiceComponents())
        loadBaseItems(store)
    }
    private fun Container.informationText(state: HippoState) {
        if (state.downloadingBaseItems) {
            div(tr("Loading ..."))
        } else if (state.errorMessage != null) {
            div(state.errorMessage)
        } else {
            div(tr("Loading completed"))
        }
    }
}

fun main() {
    startApplication(::App)
}
