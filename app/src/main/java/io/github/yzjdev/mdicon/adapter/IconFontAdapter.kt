package io.github.yzjdev.mdicon.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.yzjdev.mdicon.databinding.ItemIconBinding
import io.github.yzjdev.mdicon.model.IconItem

class IconFontAdapter(val context: Context, val items: MutableList<IconItem>, val bind: ((b: ItemIconBinding, item: IconItem, position: Int) -> Unit)? = null) : RecyclerView.Adapter<IconFontAdapter.ViewHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(ItemIconBinding.inflate(LayoutInflater.from(parent.context), parent, false))
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val item = items[position]
		bind?.invoke(holder.binding, item, position)
	}

	override fun getItemCount(): Int {
		return items.size
	}

	fun updateItems(items: List<IconItem>) {
		this.items.clear()
		this.items.addAll(items)
		notifyDataSetChanged()
	}


	inner class ViewHolder(val binding: ItemIconBinding) : RecyclerView.ViewHolder(binding.root)
}