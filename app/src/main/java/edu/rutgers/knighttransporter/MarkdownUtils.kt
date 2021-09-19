package edu.rutgers.knighttransporter

import android.content.Context
import android.graphics.Paint.FontMetricsInt
import android.text.Spanned
import android.text.style.LineHeightSpan
import android.util.DisplayMetrics
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.linkify.LinkifyPlugin
import org.commonmark.node.SoftLineBreak
import kotlin.math.roundToInt

/**
 * This was mostly copied over from my work on the 20xx-2019 Rutgers app.
 * For details about the config, see:
 * - https://github.com/noties/Markwon/issues/142
 * - https://github.com/noties/Markwon/issues/143
 */
fun createRutgersMarkwon(context: Context, autoLink: Boolean = false) = Markwon.builder(context)
    .usePlugin(object : AbstractMarkwonPlugin() {

        // Without this config, \n would just add a space, and we
        // would need to use '  \n' for one-line line breaks.
        // See https://spec.commonmark.org/0.28/#soft-line-breaks
        override fun configureVisitor(builder: MarkwonVisitor.Builder) {
            builder.on(SoftLineBreak::class.java) { visitor: MarkwonVisitor, softLineBreak: SoftLineBreak? ->
                visitor.forceNewLine()
            }
        }

//        // Add some space between list items
//        override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
//            builder.prependFactory(ListItem::class.java) { _: MarkwonConfiguration, _: RenderProps ->
//                FirstLineSpacingSpan(context, 14f)
//            }
//        }
    }).run {
        if (autoLink) usePlugin(LinkifyPlugin.create()) else this
    }
    .build()

private class FirstLineSpacingSpan(context: Context, spacingDp: Float) : LineHeightSpan {
    private val spacingPx = context.convertDpToPixel(spacingDp).roundToInt()
    private var startAscent = 0
    private var startTop = 0
    override fun chooseHeight(
        text: CharSequence,
        start: Int,
        end: Int,
        spanstartv: Int,
        v: Int,
        fm: FontMetricsInt
    ) {
        val spanStart = (text as Spanned).getSpanStart(this)
        if (start == spanStart) {
            startAscent = fm.ascent
            startTop = fm.top
            val spans = text.getSpans(
                start - 2, start,
                FirstLineSpacingSpan::class.java
            )

            // Don't add space above the first top-level item of a list
            if (spans != null && spans.isNotEmpty()) {
                fm.ascent -= spacingPx
                fm.top -= spacingPx
            }
        } else {
            fm.ascent = startAscent
            fm.top = startTop
        }
    }
}

/**
 * This method converts dp unit to equivalent pixels, depending on device density.
 *
 * From https://stackoverflow.com/a/9563438
 *
 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
 * @return A float value to represent px equivalent to dp depending on device density
 */
fun Context.convertDpToPixel(dp: Float) =
    dp * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
