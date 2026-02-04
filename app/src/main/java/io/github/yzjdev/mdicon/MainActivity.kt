package io.github.yzjdev.mdicon

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import com.caverock.androidsvg.SVG
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import io.github.yzjdev.mdicon.adapter.IconAdapter
import io.github.yzjdev.mdicon.adapter.NoFilterArrayAdapter
import io.github.yzjdev.mdicon.databinding.ActivityMainBinding
import io.github.yzjdev.mdicon.databinding.DialogColorPickerBinding
import io.github.yzjdev.mdicon.databinding.DialogIconEditBinding
import io.github.yzjdev.mdicon.exts.loadVectorDrawableFromPath
import io.github.yzjdev.mdicon.exts.loadVectorDrawableFromString
import io.github.yzjdev.mdicon.model.Icon
import io.github.yzjdev.mdicon.model.IconsMetadata
import io.github.yzjdev.utils.FileUtils
import io.github.yzjdev.utils.XmlUtils
import net.lingala.zip4j.ZipFile
import net.margaritov.preference.colorpicker.AlphaPatternDrawable
import net.margaritov.preference.colorpicker.ColorPickerPreference
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

	private val defaultExportPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
	val exportPath: String
		get() = mmkv.getString("export_path", defaultExportPath)!!

	val exportTypeId: Int
		get() = mmkv.getInt("export_type_id", R.id.button1)

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
	val filteredIcons = ArrayList<Icon>()
	lateinit var iconAdapter: IconAdapter


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
		ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}

		setSupportActionBar(binding.toolbar)

		updateEmptyState()


		if (!isStoragePermissionGranted) {
			MaterialAlertDialogBuilder(this).apply {
				setTitle("提示")
				setMessage("需要存储权限")
				setPositiveButton("申请") { _, _ ->
					requestStoragePermission()
				}
				setNegativeButton("取消", null)
				show()
			}
			return
		}

		if (!iconsMetadataFile.exists()) {
			load()
			return
		}

		setupRecyclerView()
	}

	fun load() {
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
	}

	fun requestStoragePermission() {
		XXPermissions.with(this).permission(PermissionLists.getManageExternalStoragePermission()).request { _, deniedList ->
			val allGranted = deniedList.isEmpty()
			if (allGranted) {
				if (!iconsMetadataFile.exists()) {
					load()
				} else {
					setupRecyclerView()
				}
			}
			updateEmptyState()
		}
	}

	fun setupRecyclerView() {
		val gson = Gson()
		iconsMetadata = gson.fromJson(readString(iconsMetadataFile), IconsMetadata::class.java)
		filteredIcons.clear()
		filteredIcons.addAll(iconsMetadata.icons)
		setupSpinner()
		setupSpinner2()
		iconAdapter = IconAdapter(this, filteredIcons) { b, item, position ->
			val vectorPath = item.getIconPath(storeDir, iconsMetadata.families[selectedFamily])
			b.name.text = item.name
			b.icon.loadVectorDrawableFromPath(vectorPath)
			b.root.setOnClickListener {
				BottomSheetDialog(this).apply {
					val b = DialogIconEditBinding.inflate(LayoutInflater.from(this@MainActivity))
					setContentView(b.root)
					setOnShowListener { _ ->
						window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
						findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.apply {
							BottomSheetBehavior.from(this).apply {
								skipCollapsed = true
								state = BottomSheetBehavior.STATE_EXPANDED
							}
						}
					}
					show()
					handleEditDialog(b, item, File(vectorPath))
				}
			}
		}
		binding.rv.layoutManager = GridLayoutManager(this, 4)
		binding.rv.adapter = iconAdapter
	}

	fun handleEditDialog(b: DialogIconEditBinding, item: Icon, file: File) {
		b.buttonGroup.apply {
			addOnButtonCheckedListener { group, checkedId, isChecked ->
				if (isChecked && checkedId != exportTypeId) {
					mmkv.putInt("export_type_id", checkedId)
				}
			}
			check(exportTypeId)
		}
		b.preview.apply {
			background = AlphaPatternDrawable(25)
			XmlUtils.into(this, file)
		}
		b.exportPath.editText?.apply {
			setText(exportPath)
			setSelection(text.length)
			doAfterTextChanged { text ->
				mmkv.putString("export_path", text.toString())
			}
		}
		b.name.editText?.setText(item.getName(iconsMetadata.families[selectedFamily]))
		b.color.editText?.apply {
			doAfterTextChanged { text ->
				val color = text.toString()
				val alpha = b.seekRight.text.toString()
				b.preview.loadVectorDrawableFromString(XmlUtils.change(file, color, alpha))
			}
			setOnClickListener {
				BottomSheetDialog(context).apply {
					val colorPickerBinding = DialogColorPickerBinding.inflate(LayoutInflater.from(context), null, false)
					setContentView(colorPickerBinding.root)
					setOnShowListener {
						val bottomSheet = findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
						bottomSheet?.let {
							val behavior = BottomSheetBehavior.from(it)
							behavior.isDraggable = false        // 关键：禁止下滑
							behavior.state = BottomSheetBehavior.STATE_EXPANDED
							behavior.skipCollapsed = true
						}
					}
					show()
					handleColorPickDialog(this, b, colorPickerBinding, text.toString().toColorInt())
				}

			}
		}
		b.seekAlpha.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
				val color = b.color.editText?.text.toString()
				val alpha = "${progress / 100f}"
				b.seekRight.text = alpha
				b.preview.loadVectorDrawableFromString(XmlUtils.change(file, color, alpha))
			}

			override fun onStartTrackingTouch(seekBar: SeekBar?) {

			}

			override fun onStopTrackingTouch(seekBar: SeekBar?) {

			}
		})

		b.confirm.setOnClickListener {
			val typeId = b.buttonGroup.checkedButtonId
			val suffix = when (typeId) {
				R.id.button1 -> "xml"
				R.id.button2 -> "svg"
				R.id.button3 -> "png"
				else -> "xml"
			}
			val exportDir = b.exportPath.editText?.text.toString()
			val outFile = File(exportDir, b.name.editText?.text.toString() + "." + suffix)
			val size = b.size.editText?.text.toString()
			val color = b.color.editText?.text.toString()
			val alpha = b.seekRight.text.toString()

			var str = XmlUtils.change(file, size, size, color, alpha)
			if (typeId == R.id.button2 || typeId == R.id.button3) {
				str = XmlUtils.vd2svgFromString(str)
			}
			if (typeId != R.id.button3) {
				FileUtils.writeString(outFile, str)
			} else {
				val svg = SVG.getFromString(str)
				val bitmap = createBitmap(svg.documentWidth.toInt(), svg.documentHeight.toInt())
				val canvas = Canvas(bitmap)
				canvas.drawARGB(0, 0, 0, 0)
				svg.renderToCanvas(canvas)
				FileOutputStream(outFile).use {
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
				}
			}
			"导出成功".toast(this)
		}
	}

	fun handleColorPickDialog(dialog: BottomSheetDialog, iconEditBinding: DialogIconEditBinding, b: DialogColorPickerBinding, oldColor: Int) {
		b.apply {
			hexVal.text = ColorPickerPreference.convertToRGB(oldColor)
			colorPickerView.color = oldColor
			oldColorPanel.color = oldColor
			newColorPanel.color = oldColor
			colorPickerView.setOnColorChangedListener { color ->
				hexVal.text = ColorPickerPreference.convertToRGB(color)
				newColorPanel.color = color
			}
			cancel.setOnClickListener { dialog.dismiss() }
			select.setOnClickListener {
				val color = colorPickerView.color
				iconEditBinding.color.editText?.setText(ColorPickerPreference.convertToRGB(color))
				dialog.dismiss()
			}
		}

	}

	fun setupSpinner() {
		val families = iconsMetadata.families.map { f ->
			return@map if (f == "Material Icons") {
				"Filled"
			} else {
				f.replace("Material Icons", "").replace(" ", "").trim()
			}
		}
		binding.spinner.apply {
			setAdapter(NoFilterArrayAdapter(this@MainActivity, families))
			setText(families[selectedFamily], false)
			setOnItemClickListener { parent, view, position, id ->
				val selectedItem = parent.getItemAtPosition(position) as String
				selectedFamily = position
				iconAdapter.notifyDataSetChanged()
			}
		}
	}

	fun setupSpinner2() {
		iconsMetadata.categories.add(0, "all")
		val categories = iconsMetadata.categories.map { c ->
			return@map "${c[0].uppercase()}${c.substring(1)}"
		}
		binding.spinner2.apply {
			setAdapter(NoFilterArrayAdapter(this@MainActivity, categories))
			setText(categories[selectedCategory], false)
			setOnItemClickListener { parent, view, position, id ->
				val selectedItem = parent.getItemAtPosition(position) as String
				selectedCategory = position
				filter()
			}
		}
	}


	fun filter() {
		searchExecutor.execute {
			val searchText = searchView.query.toString().trim().lowercase()
			val category = iconsMetadata.categories[selectedCategory].lowercase()

			val filteredList = iconsMetadata.icons.filter { icon ->
				// 检查分类过滤（如果选择了"all"则不过滤分类）
				val categoryMatch = category == "all" || icon.categories.any {
					it.lowercase() == category
				}

				// 检查搜索文本过滤（如果搜索文本为空则不过滤）
				val searchMatch = searchText.isEmpty() || icon.name.lowercase().contains(searchText) || icon.tags.any { it.lowercase().contains(searchText) }

				// 同时满足分类和搜索条件
				categoryMatch && searchMatch
			}

			uiHandler.post {
				iconAdapter.updateItems(filteredList)
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
		searchItem?.isVisible = XXPermissions.isGrantedPermission(this, PermissionLists.getManageExternalStoragePermission())
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
			R.id.action_help -> {
				if (!isStoragePermissionGranted) {
					MaterialAlertDialogBuilder(this).apply {
						setTitle("提示")
						setMessage("需要存储权限")
						setPositiveButton("申请") { _, _ ->
							requestStoragePermission()
						}
						setNegativeButton("取消", null)
						show()
					}
				}
			}

			R.id.action_al_iconfont -> startActivity(Intent(this, IconFontActivity::class.java))
		}
		return super.onOptionsItemSelected(item)
	}

	val isStoragePermissionGranted: Boolean
		get() = XXPermissions.isGrantedPermission(this, PermissionLists.getManageExternalStoragePermission())


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
		val empty = !iconsMetadataFile.exists() || !isStoragePermissionGranted
		binding.emptyContainer.visibility = if (empty) View.VISIBLE else View.GONE
		binding.mainContainer.visibility = if (empty) View.GONE else View.VISIBLE
	}

}

class SimpleStringAdapter(
	context: Context, private val data: List<String>
) : BaseAdapter() {

	private val inflater = LayoutInflater.from(context)

	override fun getCount() = data.size
	override fun getItem(position: Int) = data[position]
	override fun getItemId(position: Int) = position.toLong()

	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val view = convertView ?: inflater.inflate(
			android.R.layout.simple_spinner_dropdown_item, parent, false
		)
		(view as TextView).text = data[position]
		return view
	}
}


fun readString(file: File) = file.readText(charset = StandardCharsets.UTF_8)
