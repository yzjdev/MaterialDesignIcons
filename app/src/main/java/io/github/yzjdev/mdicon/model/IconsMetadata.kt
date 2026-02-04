package io.github.yzjdev.mdicon.model

import com.google.gson.annotations.SerializedName
import java.io.File

data class IconsMetadata(
	val families: List<String>, val categories: ArrayList<String>, val icons: ArrayList<Icon>
)


data class Icon(
	val name: String,
	@SerializedName("unsupported_families")
	val unsupportedFamilies: List<String>,
	val categories: List<String>,
	val tags: List<String>
) {

	fun getName(family: String): String {
		val prefix = when {
			family == "Material Icons" -> "baseline"
			family.endsWith("Outlined") -> "outline"
			else -> family.replace("Material Icons", "").replace(" ", "").trim().lowercase()
		}

		return "${prefix}_${name}_24"
	}
	fun getIconPath(storeDir: File, family: String): String {
		val prefix = when {
			family == "Material Icons" -> "baseline"
			family.endsWith("Outlined") -> "outline"
			else -> family.replace("Material Icons", "").replace(" ", "").trim().lowercase()
		}

		val familyDirName = family.replace(" ", "").lowercase()
		val fileName = "${prefix}_${name}_24.xml"

		return File(storeDir, "$familyDirName/$name/$fileName").absolutePath
	}
}
