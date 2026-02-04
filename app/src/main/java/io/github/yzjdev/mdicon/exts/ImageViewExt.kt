package io.github.yzjdev.mdicon.exts

import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.view.View
import android.widget.ImageView
import com.caverock.androidsvg.SVG
import io.github.yzjdev.utils.XmlUtils
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

fun ImageView.loadVectorDrawableFromString(vectorString: String) {
	val svg = SVG.getFromString(XmlUtils.vd2svgFromString(vectorString))
	val pic = svg.renderToPicture()
	loadVectorDrawable(PictureDrawable(pic))
}

fun ImageView.loadVectorDrawableFromPath(vectorPath: String) {
	loadVectorDrawable(File(vectorPath))
}

fun ImageView.loadVectorDrawable(vectorFile: File) {
	loadVectorDrawable(FileInputStream(vectorFile))
}

fun ImageView.loadVectorDrawable(input: InputStream) {
	val svg = SVG.getFromString(XmlUtils.vd2svg(input))
	val pic = svg.renderToPicture()
	loadVectorDrawable(PictureDrawable(pic))
}

fun ImageView.loadVectorDrawable(drawable: Drawable) {
	setLayerType(View.LAYER_TYPE_SOFTWARE, null)
	setImageDrawable(drawable)
}

fun ImageView.loadSvgFromString(svgString: String) {
	val svg = SVG.getFromString(svgString)
	val pic = svg.renderToPicture()
	loadVectorDrawable(PictureDrawable(pic))
}

