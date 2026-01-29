package io.github.yzjdev.mdicon

import android.graphics.drawable.PictureDrawable
import android.view.View
import android.widget.ImageView
import com.caverock.androidsvg.SVG
import io.github.yzjdev.svg2vector.Vector2Svg
import java.io.File
import java.io.FileInputStream

fun ImageView.loadVectorDrawable(vectorFile: File) {
    val str = Vector2Svg.parseXmlToSvg(FileInputStream(vectorFile))
    str.log()
    val svg = SVG.getFromString(str)
    val pic = svg.renderToPicture()
    val drawable = PictureDrawable(pic)
    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    setImageDrawable(drawable)
}

