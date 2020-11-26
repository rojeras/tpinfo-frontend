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

enum class View(val url: String) {
    HOME("/"),
    HIPPO("/hippo"),
    STAT_SIMPLE("/statsimple"),
    STAT_ADVANCED("/statadvanced")
}

fun Navigo.initialize(): Navigo {
    return on(
        View.HOME.url,
        { _ -> HippoManager.newOrUpdatedUrlFromBrowser(View.HOME) }
    ) /* .on(
        "${View.HOME.url}/:slug",
        { params -> HippoManager.newOrUpdatedUrlFromBrowser(View.HOME, stringParameter(params, "slug")) }
    ) */.on(
        View.HIPPO.url,
        { _ -> HippoManager.newOrUpdatedUrlFromBrowser(View.HIPPO) }
    ).on(
        "${View.HIPPO.url}/:slug",
        { params -> HippoManager.newOrUpdatedUrlFromBrowser(View.HIPPO, stringParameter(params, "slug")) }
    ).on(
        View.STAT_SIMPLE.url,
        { _ -> HippoManager.newOrUpdatedUrlFromBrowser(View.STAT_SIMPLE) }
    ).on(
        "${View.STAT_SIMPLE.url}/:slug",
        { params -> HippoManager.newOrUpdatedUrlFromBrowser(View.STAT_SIMPLE, stringParameter(params, "slug")) }
    ).on(
        View.STAT_ADVANCED.url,
        { _ -> HippoManager.newOrUpdatedUrlFromBrowser(View.STAT_ADVANCED) }
    ).on(
        "${View.STAT_ADVANCED.url}/:slug",
        { params -> HippoManager.newOrUpdatedUrlFromBrowser(View.STAT_ADVANCED, stringParameter(params, "slug")) }
    )
}

fun stringParameter(params: dynamic, parameterName: String): String {
    return (params[parameterName]).toString()
}
