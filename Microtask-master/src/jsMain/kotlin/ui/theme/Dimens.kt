package ui.theme

import org.jetbrains.compose.web.css.CSSKeywordValue
import org.jetbrains.compose.web.css.keywords.CSSAutoKeyword

object Dimens {
    fun toPercentage(size: Int) = CSSKeywordValue("$size%").unsafeCast<CSSAutoKeyword>()
}