package se.skoview.view

import pl.treksoft.kvision.core.Background
import pl.treksoft.kvision.core.Col
import pl.treksoft.kvision.core.Color
import pl.treksoft.kvision.core.Style
import pl.treksoft.kvision.utils.rem

val formControlXs = Style {
    setStyle("height", "calc(1.3em + .4rem + 2px)")
    setStyle("padding", "0.2rem 0.4rem")
    fontSize = 0.7.rem
    lineHeight = 0.6.rem
    // todo: Try to make this setting to work:
    background = Background(Color.name(Col.WHITE))
}