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
import se.skoview.view.*
import kotlin.browser.window

// Common

// todo: Show messages to user
// todo: Make it possible to participate in discussion, maybe through slack channel
// todo: Verifiera att zip bygger en produktionsversion
// todo: Visa antal användare senaste 24 timmarna
// todo: Lägg in stöd för Navigo routing
// todo: Börja använda Karma och enhetstester

// Hippo

// todo: Opera does not add any filter to URL, remove its mention in index.html
// todo: Check link to statistics
// todo: Make it possible to see diffs, that is, changes between certain dates (from John)
// todo: Check if it is possible to make each column in hippo scrollable - without showing a scrollbar
// todo: Lös detta med att visa SE för vägval
// todo: Tag fram mock för hur integrationer ska presenteras där det kan finnas flera LA
// todo: Lös trädklättringen, kanske mha HSA-trädet
// todo: Titta på Tabulator igen

// Statistik

// todo: Fixa till datahanteringen så att det blir en renare redux-koppling till vad som visas
// todo: Fixa skärmuppdateringen så att det inte blinkar och försvinner ibland
// todo: Addera vy för "Över tid"
// todo: Export till CSV
// todo: Fixa "about" för statistiken
// todo: Inkludera synonymer
// todo: Förvalda vyer som i gamla statistiken
// done: Visa HSA-idn (sökbara)

// Done

// done: Skriv ut versionsnummer på sidan
// done: Add the initial loading of integrations and stat data to be on demand from the respective view - not from areAllBaseItemsLoaded()
// done: Bug: Selected dates not included in URL filter
// done: Fritextsökningen måste snabbas upp
// done: Addera licensinformationen
// done: Fixa BASEURL så att den inte är hårdkodad mot tpinfo-a
// done: Fixa så det går att kopiera text utan att itemet väljs bort
// done: Lite hjälptexter, troligen på egen sida (via knapp ev)
// done: Testa i andra webbläsare och Win/Linux
// done: Höjden på datumraden
// done: Kolumnerna ändrar fortfarande bredd
// done: Fixa val och fritextsökning av plattformChains
// done: Fixa till URL-hanteringen
// done: Fixa så att cursorn anpassar sig
// done: Stödtext efter item 100
// done: Snygga till ramarna och marginalerna
// done: Byta plats på logiska adresser och producenter
// done: Fix bug reported by Annika about error in filtering. Due to getParams() should only return dates.
// done: Bug in url handling. http//hippokrates.se/hippo - it seems hippo is removed
// done: Gå igenom all kod, städa och refaktorera det viktigaste
// done: Check why the icon is not displayed when run from hippokrates.se
// done: Länk till statistiken
// done: Fixa till färgerna
// done: Aktivera Google Analytics
// done: Publicera rhippo på hippokrates.se
// done: Markera om ingen träff i sökning på nåt sätt
// Och efter produktionssättningen, i 1.1
// done: Förbättre svarstiderna för fritextsökningen
// done: Färger på rubrikerna
// done: Frys rubrikraden
// done: Lägg till knapp som tar bort gränsen över hur många items som visar. Dvs en "visa alla"-knapp som dyker upp isf den röda texten.
// UJ kommentarer:
// done: Sidan är större än tidigare version – måste bli lika bred som tidigare version.
// done: Texterna inom rutorna ligger för nära ramarna.
// done: Felstavningar i ”Om hippo”, ändra ”hippo” till ”Hippo”.
// done: Varför poppar rutan ”SLL statistiktjänst” upp – finns väl ingen anledning till det.
// done: ”Återställ tjänsteplattform(ar)” bör flyttas ned någon centimeter.

// Initialize the redux store
val store = createReduxStore(
    ::hippoReducer,
    getInitialState()
)

fun main() {
    startApplication(::App)
}

class App : Application() {
    init {
        require("css/hippo.css")
    }

    override fun start() {
        val startUrl = window.location.href
        println("window.location.href: $startUrl")

        // If hostname or path contains "statistik" then we start the statistik app, else hippo
        val isStatApp =
            startUrl.contains("statistik")

        // A listener that sets the URL after each state change
        store.subscribe { state ->
            setUrlFilter(state)
        }

        Pace.init()
        loadBaseItems(store)

        store.subscribe { state ->
            if (state.currentAction == HippoAction.DoneDownloadBaseItems::class) {
                if (isStatApp) startStat()
                else startHippo()
            }
        }
    }

    fun startHippo() {
        store.dispatch(HippoAction.ApplicationStarted(HippoApplication.HIPPO))

        loadIntegrations(store.getState())
        root("hippo") {
            vPanel {
                //add(HippoTablePage)
                add(HippoTablePage)
            }.apply {
                width = 100.perc
            }

        }
    }

    fun startStat() {
        store.dispatch(HippoAction.ApplicationStarted(HippoApplication.STATISTIK))
        loadStatistics(store.getState())
        root("hippo") {
            vPanel {
                add(StatPage)
            }.apply {
                width = 100.perc
            }

        }
    }

}
