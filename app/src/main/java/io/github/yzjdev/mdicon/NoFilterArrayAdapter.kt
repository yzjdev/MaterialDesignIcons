package io.github.yzjdev.mdicon

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class NoFilterArrayAdapter<T>(
	context: Context, resource: Int, private val items: List<T>
) : ArrayAdapter<T>(context, resource, items) {

	constructor(context: Context, items: List<T>) : this(context, android.R.layout.simple_spinner_dropdown_item, items)

	override fun getFilter(): Filter {
		return NoFilter()
	}

	private inner class NoFilter : Filter() {
		override fun performFiltering(constraint: CharSequence?): FilterResults {
			// 直接返回原始结果，无视输入的 constraint
			val results = FilterResults()
			results.values = items
			results.count = items.size
			return results
		}

		override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
			// 关键点：不调用 ArrayAdapter 默认的 setData 或 clear/add 逻辑
			// 而是简单地通知数据集未发生变化（或已刷新）
			// 这样列表就会一直显示所有原始 items
			notifyDataSetChanged()
		}
	}
}
