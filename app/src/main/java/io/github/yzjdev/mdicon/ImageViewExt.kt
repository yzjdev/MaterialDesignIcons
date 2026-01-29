package io.github.yzjdev.mdicon

import android.R.attr.path
import android.graphics.drawable.PictureDrawable
import android.view.View
import android.widget.ImageView
import com.caverock.androidsvg.SVG
import java.io.File

fun ImageView.loadVectorDrawable(vectorDrawablePath: String) {
    val converter = Vd2SvgConverter()
    val svgString = converter.convert(File(vectorDrawablePath))
    val svg = SVG.getFromString(svgString)
    val pic = svg.renderToPicture()
    val drawable = PictureDrawable(pic)
    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    setImageDrawable(drawable)
}