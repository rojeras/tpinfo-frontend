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
package se.skoview.common

import pl.treksoft.navigo.Navigo

/**
 * The following URL patterns can occur and must be handled
 *
 * Legacy pattern:
 *
 *  integrationer.tjansteplattform.se  ->  integrationer.tjansteplattform.se/hippo/#/hippo  OK
 *  integrationer.tjansteplattform.se/hippo  ->  integrationer.tjansteplattform.se/hippo/#/hippo OK
 *  integrationer.tjansteplattform.se/hippo/?filter=C40 -> https://integrationer.tjansteplattform.se/hippo/#/hippo/?filter=C40 [integrationer.tjansteplattform.se/hippo/?filter=C40#/hippo]
 *
 *  statistik.tjansteplattform.se -> statistik.tjansteplattform.se/stat/#/stat/filter=D1P1 [statistik.tjansteplattform.se/stat/#/stat]
 *  statistik.tjansteplattform.se/?filter=S1723E1753L3i1&service=consumers -> statistik.tjansteplattform.se/stat/#/stat/filter=S1723E1753F3L3D1P1 [statistik.tjansteplattform.se/stat/#/stat?filter=S1723E1753L3i1&service=consumers]
 *
 * New pattern:
 *  integrationer.tjansteplattform.se/hippo/#/hippo
 *  integrationer.tjansteplattform.se/hippo/#/hippo/filter=C40D1D2D3D4P0
 *  statistik.tjansteplattform.se/stat/#/hippo/[filter=C40D1D2D3D4P0]
 *
 *  statistik.tjansteplattform.se/stat/#/stat
 *  statistik.tjansteplattform.se/stat/#/stat/filter=S1723E1753c865F3L3D1D2D3D4P0
 *  integrationer.tjansteplattform.se/hippo/#/stat/[filter=S1723E1753F3L3D1D2D3D4P0]
 *
 */

enum class View(val url: String) {
    HOME("/"),
    HIPPO("/hippo"),
    STAT("/stat"),
    // STAT_ADVANCED("/statadvanced")
}

fun Navigo.initialize(): Navigo {
    return on(
        View.HOME.url,
        { _ -> HippoManager.newOrUpdatedUrlFromBrowser(view = View.HOME, origin = "home") }
    ) /* .on(
        "${View.HOME.url}/:slug",
        { params -> HippoManager.newOrUpdatedUrlFromBrowser(View.HOME, stringParameter(params, "slug"), origin = "home-slug") }
    ) */.on(
        View.HIPPO.url,
        { _ -> HippoManager.newOrUpdatedUrlFromBrowser(view = View.HIPPO, origin = "hippo") }
    ).on(
        "${View.HIPPO.url}/:slug",
        { params -> HippoManager.newOrUpdatedUrlFromBrowser(View.HIPPO, stringParameter(params, "slug"), origin = "hippo-slug") }
    ).on(
        View.STAT.url,
        { _ -> HippoManager.newOrUpdatedUrlFromBrowser(View.STAT) }
    ).on(
        "${View.STAT.url}/:slug",
        { params -> HippoManager.newOrUpdatedUrlFromBrowser(View.STAT, stringParameter(params, "slug"), origin = "stat-slug") }
    )
        /*
        .on(
        View.STAT_ADVANCED.url,
        { _ -> HippoManager.newOrUpdatedUrlFromBrowser(View.STAT_ADVANCED) }
    ).on(
        "${View.STAT_ADVANCED.url}/:slug",
        { params -> HippoManager.newOrUpdatedUrlFromBrowser(View.STAT_ADVANCED, stringParameter(params, "slug")) }
    )
         */
}

fun stringParameter(params: dynamic, parameterName: String): String {
    return (params[parameterName]).toString()
}
