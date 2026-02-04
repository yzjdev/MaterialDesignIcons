package io.github.yzjdev.mdicon.model

import com.google.gson.annotations.SerializedName

data class IconFont(val data: Data)

data class Data(
	val count: Int,
	val limit: Int,
	val page: Int,
	val sort: String,
	@SerializedName("lists")
	val collections: List<IconCollection>
)

data class IconCollection(
	@SerializedName("User")
	val user: UserInfo,
	val icons: List<IconItem>,
	@SerializedName("all_count")
	val allCount: Int,
	@SerializedName("icons_count")
	val iconsCount: Int,
	val copyright: String,
	@SerializedName("create_user_id")
	val createUserId: String,
	@SerializedName("created_at")
	val createTime: String,
	val description: String,
	val name: String,
	val id: String,

	val slug: String, //category
	val type: String,
	@SerializedName("updated_at")
	val updateTime: String,
	@SerializedName("tag_ids")
	val tagIds : String
)


data class IconItem(
	val name: String,
	@SerializedName("show_svg")
	val svgString: String,
	@SerializedName("updated_at")
	val updateTime: String,
)

data class UserInfo(val avatar: String, val id: String, val nickname: String, val nid: String)


data class SearchResult(val data: SearchData)
data class SearchData(val icons: List<IconItem>, val count: Int)