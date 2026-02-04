package io.github.yzjdev.mdicon.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.yzjdev.mdicon.databinding.ItemIconBinding
import io.github.yzjdev.mdicon.model.Icon

class IconAdapter(val context: Context, val items: MutableList<Icon>, val bind: ((b: ItemIconBinding, item: Icon, position: Int) -> Unit)? = null) : RecyclerView.Adapter<IconAdapter.ViewHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val layoutInflater = LayoutInflater.from(context)
		val binding = ItemIconBinding.inflate(layoutInflater, parent, false)
		return ViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		bind?.invoke(holder.b, items[position], position)
	}



	override fun getItemCount(): Int {
		return items.size
	}

	fun updateItems(items: List<Icon>) {
		this.items.clear()
		this.items.addAll(items)
		notifyDataSetChanged()
	}


	inner class ViewHolder(val b: ItemIconBinding) : RecyclerView.ViewHolder(b.root)
}