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
        { _ -> HippoManager.fromUrl(View.HOME) }
    ).on(
        "${View.HIPPO.url}/:slug",
        { params -> HippoManager.fromUrl(View.HIPPO, stringParameter(params, "slug")) }
    ).on(
        "${View.STAT_SIMPLE.url}/:slug",
        { params -> HippoManager.fromUrl(View.STAT_SIMPLE, stringParameter(params, "slug")) }
    ).on(
        "${View.STAT_ADVANCED.url}/:slug",
        { params -> HippoManager.fromUrl(View.STAT_ADVANCED, stringParameter(params, "slug")) }
    )
}
/*
fun Navigo.initialize(): Navigo {
    return on(View.HOME.url, { _ ->
        ConduitManager.homePage()
    }).on("${View.ARTICLE.url}/:slug", { params ->
        ConduitManager.showArticle(stringParameter(params, "slug"))
    }).on(RegExp("^${View.PROFILE.url}([^/]+)$"), { username ->
        ConduitManager.showProfile(username, false)
    }).on(RegExp("^${View.PROFILE.url}([^/]+)/favorites$"), { username ->
        ConduitManager.showProfile(username, true)
    }).on(View.LOGIN.url, { _ ->
        ConduitManager.loginPage()
    }).on(View.REGISTER.url, { _ ->
        ConduitManager.registerPage()
    }).on(View.SETTINGS.url, { _ ->
        ConduitManager.settingsPage()
    }).on(View.EDITOR.url, { _ ->
        ConduitManager.editorPage()
    }).on("${View.EDITOR.url}/:slug", { params ->
        ConduitManager.editorPage(stringParameter(params, "slug"))
    })
}
 */


fun stringParameter(params: dynamic, parameterName: String): String {
    return (params[parameterName]).toString()
}
