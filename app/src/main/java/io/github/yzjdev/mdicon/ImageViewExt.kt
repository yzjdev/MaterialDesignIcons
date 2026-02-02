package io.github.yzjdev.mdicon

import android.graphics.drawable.PictureDrawable
import android.view.View
import android.widget.ImageView
import com.caverock.androidsvg.SVG
import io.github.yzjdev.utils.XmlUtils
import java.io.File

fun ImageView.loadVectorDrawable(vectorFile: File) {
	try {
		val str = XmlUtils.vd2svg(vectorFile)
		val svg = SVG.getFromString(str)
		val pic = svg.renderToPicture()
		val drawable = PictureDrawable(pic)
		setLayerType(View.LAYER_TYPE_SOFTWARE, null)
		setImageDrawable(drawable)
	} catch (e: Exception) {
	}
}

