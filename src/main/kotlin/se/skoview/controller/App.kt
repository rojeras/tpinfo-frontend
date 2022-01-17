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
package se.skoview.controller

import io.kvision.*
import io.kvision.core.Overflow
import io.kvision.panel.root
import io.kvision.routing.Routing
import se.skoview.controller.HippoManager.mainLoop

/**
 * Global debug support. Turn on different background colors for most containers.
 */
val showBackgroundColorsForDebug: Boolean = false

/**
 * Entry point of application.
 */
fun main() {
    startApplication(
        ::App,
        module.hot,
        BootstrapModule,
        BootstrapCssModule,
        FontAwesomeModule,
        BootstrapSelectModule,
        BootstrapDatetimeModule,
        BootstrapUploadModule,
        CoreModule,
        panelsCompatibilityMode = true
    ) // startApplication(::App, module.hot)
}

/**
 * Initialize and startup..
 * Enter the main loop (in [HippoManager]).
 */
class App : Application() {
    init {
        require("css/hippo.css")
    }
    override fun start() {
        Routing.init()
        HippoManager.initialize()

        root("hippo") {
            overflow = Overflow.HIDDEN
            mainLoop() // In HippoManager
        }
    }
}


