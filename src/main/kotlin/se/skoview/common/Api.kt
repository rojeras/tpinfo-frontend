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

import kotlinx.browser.window
import pl.treksoft.kvision.rest.RestClient

object Api {

    val API_URL = getApiUrl()

    private val restClient = RestClient()
/*
    suspend fun statPlattforms(username: String): User {
        return restClient.call<ProfileDto>(
            "$API_URL/profiles/$username",
            beforeSend = ::authRequest
        ).await().profile
    }
*/
    private fun getApiUrl(): String {
        val currentProtocol = window.location.protocol
        val currentHost = window.location.host
        // tpdb is assumed to be on the 'qa.integrationer.tjansteplattform.se' server if we run in development or test environment
        return if (currentHost.contains("localhost") || currentHost.contains("192.168.0.") || currentHost.contains("www.hippokrates.se")) {
            "https://qa.integrationer.tjansteplattform.se/tpdb/tpdbapi.php/api/v1/"
        } else {
            "$currentProtocol//$currentHost/../tpdb/tpdbapi.php/api/v1/"
        }
    }
}
