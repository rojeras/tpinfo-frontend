package test.se.skoview

import pl.treksoft.kvision.test.SimpleSpec
import se.skoview.common.HippoState
import kotlin.test.Test
import kotlin.test.assertTrue

class AppSpec : SimpleSpec {

    @Test
    fun render() {
        run {
            assertTrue(true, "Dummy test")
        }
    }

    @Test
    fun check_hippostate() {
        run {
            val state = HippoState()
            assertTrue { true }
        }
    }
}
