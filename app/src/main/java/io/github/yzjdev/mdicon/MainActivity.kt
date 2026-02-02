package io.github.yzjdev.mdicon

import android.content.Context
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import io.github.yzjdev.mdicon.databinding.ActivityMainBinding
import io.github.yzjdev.mdicon.databinding.DialogColorPickerBinding
import io.github.yzjdev.mdicon.databinding.DialogIconEditBinding
import net.lingala.zip4j.ZipFile
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class MainActivity : AppCompatActivity() {
	companion object {
		const val MSG_SHOW_PROGRESS = 1
		const val MSG_UPDATE_PROGRESS = 2
		const val MSG_DISMISS_PROGRESS = 3
	}

	private val unzipExecutor = Executors.newSingleThreadExecutor()
	private val searchExecutor = Executors.newSingleThreadExecutor()
	private val uiHandler = object : android.os.Handler(android.os.Looper.getMainLooper()) {
		override fun handleMessage(msg: Message) {
			super.handleMessage(msg)
			when (msg.what) {
				MSG_SHOW_PROGRESS -> {
					if (!progressDialog.isShowing) progressDialog.show()
				}

				MSG_UPDATE_PROGRESS -> {
//                    progressDialog.setMessage(msg.obj as String)
				}

				MSG_DISMISS_PROGRESS -> {
					if (progressDialog.isShowing) progressDialog.dismiss()
				}
			}
		}
	}
	val TAG = "aaa"
	private lateinit var binding: ActivityMainBinding
	lateinit var iconsMetadata: IconsMetadata
	lateinit var adapter: IconAdapter
	lateinit var filteredIcons: ArrayList<Icon>
	val storeDir: File
		get() {
			val f = File(App.instance.getExternalFilesDir(null), "icons/material")
			if (!f.exists()) f.mkdirs()
			return f
		}
	val iconsMetadataFile = File(storeDir, "icons_metadata.txt")
	val zipFile = File(storeDir, "material.zip")

	lateinit var progressDialog: AlertDialog

	lateinit var searchView: SearchView
	private var selectedFamily = 0
	private var selectedCategory = 0


	var isWorking = false

	@OptIn(ExperimentalAtomicApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}

		setSupportActionBar(binding.toolbar)

		updateEmptyState()
		XXPermissions.with(this).permission(PermissionLists.getManageExternalStoragePermission()).request(null)
		if (!iconsMetadataFile.exists()) {
			createProgressDialog()
			unzipExecutor.execute {
				isWorking = true
				var ok = false
				try {
					uiHandler.sendEmptyMessage(MSG_SHOW_PROGRESS)
					unzip()
					ok = true
				} catch (e: Exception) {
				} finally {
					isWorking = false
					uiHandler.sendEmptyMessage(MSG_DISMISS_PROGRESS)
					if (ok) {
						uiHandler.post {
							updateEmptyState()
							setupRecyclerView()
						}
					}
				}
			}
			return
		}


		setupRecyclerView()
	}

	fun setupRecyclerView() {
		val gson = Gson()
		iconsMetadata = gson.fromJson(readString(iconsMetadataFile), IconsMetadata::class.java)

		setupSpinner()
		setupSpinner2()

		adapter = IconAdapter(this, iconsMetadata.icons) { b, item, position ->
			val file = File(item.getIconPath(storeDir, iconsMetadata.families[selectedFamily]))
			b.name.text = item.name
			b.icon.loadVectorDrawable(file)
			b.root.setOnClickListener {
				BottomSheetDialog(this).apply {
					val b = DialogIconEditBinding.inflate(LayoutInflater.from(this@MainActivity))
					setContentView(b.root)
					setOnShowListener { _ ->
						window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
					}
					show()
					handleEditDialog(b, item, file)
				}
			}
		}
		binding.rv.layoutManager = GridLayoutManager(this, 4)
		binding.rv.adapter = adapter
	}

	fun handleEditDialog(b: DialogIconEditBinding, item: Icon, file: File) {
		b.color.editText?.apply {
			setOnClickListener {
				MaterialAlertDialogBuilder(context).apply {
					val b = DialogColorPickerBinding.inflate(LayoutInflater.from(context))
					setTitle("颜色选择")
					setView(b.root)
					setPositiveButton("选择", null)
					setNegativeButton("取消", null)
					show()
				}
			}
		}
	}

	fun setupSpinner() {
		val families = iconsMetadata.families.map { f ->
			if (f == "Material Icons") {
				return@map "Filled"
			} else {
				return@map f.replace("Material Icons", "").replace(" ", "").trim()
			}
		}
		val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, families) {
			override fun getFilter(): Filter {
				return object : Filter() {
					override fun performFiltering(constraint: CharSequence?): FilterResults {
						return FilterResults().apply {
							values = families
							count = families.size
						}
					}

					override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
						notifyDataSetChanged()
					}

				}
			}
		}
		binding.spinner.apply {
			setAdapter(adapter)
			setText(families[selectedFamily], false)
			setOnItemClickListener { parent, view, position, id ->
				val selectedItem = parent.getItemAtPosition(position) as String
				selectedFamily = position
				filter()
			}
		}
	}

	fun setupSpinner2() {
		iconsMetadata.categories.add(0, "all")
		val categories = iconsMetadata.categories.map { c ->
			return@map "${c[0].uppercase()}${c.substring(1)}"
		}
		val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categories) {
			override fun getFilter(): Filter {
				return object : Filter() {
					override fun performFiltering(constraint: CharSequence?): FilterResults {
						return FilterResults().apply {
							values = categories
							count = categories.size
						}
					}

					override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
						notifyDataSetChanged()
					}

				}
			}
		}
		binding.spinner2.apply {
			setAdapter(adapter)
			setText(categories[selectedFamily], false)
			setOnItemClickListener { parent, view, position, id ->
				val selectedItem = parent.getItemAtPosition(position) as String
				selectedFamily = position
				filter()
			}
		}
	}


	fun filter() {
		searchExecutor.execute {
			val searchText = searchView.query.toString().trim().lowercase()
			val family = iconsMetadata.families[selectedFamily]
			val category = iconsMetadata.categories[selectedCategory]
			val isSearchEmpty = TextUtils.isEmpty(searchText)
			val isAllFamily = "all" == family
			val isAllCategory = "all" == category
			val list = iconsMetadata.icons.filter { icon ->
				if (!isAllFamily) {
					if (icon.unsupportedFamilies.contains(family)) {
						return@filter false
					}
				}

				if (!isAllCategory) {
					if (!icon.categories.contains(category)) return@filter false
				}

				if (!isSearchEmpty) {
					val nameMatch = icon.name.lowercase().contains(searchText)
					var tagsMatch = false
					if (!nameMatch) {
						for (tag in icon.tags) {
							if (tag.lowercase().contains(searchText)) {
								tagsMatch = true
								break
							}
						}
					}

					if (!nameMatch && !tagsMatch) return@filter false
				}
				return@filter true
			}
			uiHandler.post {
				adapter.updateItems(list)
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		uiHandler.removeCallbacksAndMessages(null)
		unzipExecutor.shutdownNow()
		searchExecutor.shutdownNow()
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.main_menu, menu)
		val searchItem = menu?.findItem(R.id.action_search)
		searchView = searchItem?.actionView as SearchView
		searchView.queryHint = "搜索图标..."
		searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(query: String?): Boolean {
				filter()
				return false
			}

			override fun onQueryTextChange(newText: String?): Boolean {
				filter()
				return false
			}
		})
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_help -> {}
		}
		return super.onOptionsItemSelected(item)
	}


	fun createProgressDialog() {
		MaterialAlertDialogBuilder(this).apply {
			setView(R.layout.dialog_progress)
			progressDialog = create()
		}
	}

	fun unzip() {
		assets.open("icons/material.zip").use { input ->
			FileOutputStream(zipFile).use { output ->
				val buffer = ByteArray(8 * 1024)
				var len: Int
				while (input.read(buffer).also { len = it } != -1) {
					output.write(buffer, 0, len)
				}
			}
		}
		ZipFile(zipFile).extractAll(storeDir.absolutePath)
		zipFile.delete()
	}

	fun updateEmptyState() {
		val empty = !iconsMetadataFile.exists()
		binding.emptyContainer.visibility = if (empty) View.VISIBLE else View.GONE
		binding.mainContainer.visibility = if (empty) View.GONE else View.VISIBLE
	}

}

class SimpleStringAdapter(
	context: Context,
	private val data: List<String>
) : BaseAdapter() {

	private val inflater = LayoutInflater.from(context)

	override fun getCount() = data.size
	override fun getItem(position: Int) = data[position]
	override fun getItemId(position: Int) = position.toLong()

	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val view = convertView ?: inflater.inflate(
			android.R.layout.simple_spinner_dropdown_item,
			parent,
			false
		)
		(view as TextView).text = data[position]
		return view
	}
}


fun readString(file: File) = file.readText(charset = StandardCharsets.UTF_8)
