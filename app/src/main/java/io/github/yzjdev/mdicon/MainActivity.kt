package io.github.yzjdev.mdicon

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.github.yzjdev.mdicon.databinding.ActivityMainBinding
import io.github.yzjdev.mdicon.databinding.ItemIconBinding
import net.lingala.zip4j.ZipFile
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    companion object {
        const val MSG_SHOW_PROGRESS = 1
        const val MSG_UPDATE_PROGRESS = 2
        const val MSG_DISMISS_PROGRESS = 3
    }

    private val unzipExecutor: ExecutorService = Executors.newSingleThreadExecutor()
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
                    updateEmptyState()
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
        if (!iconsMetadataFile.exists()) {
            createProgressDialog()
            unzipExecutor.execute {
                uiHandler.sendEmptyMessage(MSG_SHOW_PROGRESS)
                unzip()
                uiHandler.sendEmptyMessage(MSG_DISMISS_PROGRESS)
            }
            return
        }

        val gson = Gson()
        val json = readString(iconsMetadataFile)
        iconsMetadata = gson.fromJson(json, IconsMetadata::class.java)
        filteredIcons = ArrayList(iconsMetadata.icons)
        setupSpinner()
        setupSpinner2()
        setupRecyclerView()
    }

    fun setupRecyclerView() {
        adapter = IconAdapter(filteredIcons)
        binding.rv.layoutManager = GridLayoutManager(this, 4)
        binding.rv.adapter = adapter
    }

    fun setupSpinner() {
        val families = iconsMetadata.families.map { f ->
            if (f == "Material Icons") {
                return@map "Filled"
            } else {
                return@map f.replace("Material Icons", "").replace(" ", "").trim()
            }
        }
        val adapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, families)
        binding.spinner.setAdapter(adapter)
        binding.spinner.setText(families[selectedFamily], false)
        binding.spinner.setOnItemClickListener { _, _, position, _ ->
            selectedFamily = position
            filter()
        }
    }

    fun setupSpinner2() {
        iconsMetadata.categories.add(0, "all")
        val categories = iconsMetadata.categories.map { c ->
            return@map "${c[0].uppercase()}${c.substring(1)}"
        }
        val adapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categories)
        binding.spinner2.setAdapter(adapter)
        binding.spinner2.setText(categories[selectedCategory], false)
        binding.spinner2.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = position
            filter()
        }
    }


    fun filter() {
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
        adapter.updateItems(list)
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
    }


    fun updateEmptyState() {
        val empty = !iconsMetadataFile.exists()
        binding.emptyContainer.visibility = if (empty) View.VISIBLE else View.GONE
        binding.mainContainer.visibility = if (empty) View.GONE else View.VISIBLE
    }

    var copyText = ""

    inner class IconAdapter(val items: MutableList<Icon> = ArrayList()) :
        RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding =
                ItemIconBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        @SuppressLint("ServiceCast")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val iconPath = item.getIconPath(storeDir, iconsMetadata.families[selectedFamily])

            holder.binding.name.text = item.name
            holder.binding.icon.loadVectorDrawable(iconPath)

            holder.itemView.setOnClickListener { v ->

                // 显示popup menu
                val popupMenu = PopupMenu(this@MainActivity, v)
                popupMenu.setForceShowIcon(true)

                // 动态创建菜单项
//                popupMenu.menu.add(0, 1, 0, "导入项目").setIcon(R.drawable.outline_file_open_24)
                popupMenu.menu.add(0, 2, 0, "导出为XML").setIcon(R.drawable.outline_code_24)
                popupMenu.menu.add(0, 3, 0, "导出为SVG").setIcon(R.drawable.outline_draw_24)
                popupMenu.menu.add(0, 4, 0, "导出为PNG").setIcon(R.drawable.outline_image_24)
                popupMenu.menu.add(0, 5, 0, "编辑").setIcon(R.drawable.outline_edit_24)

                val builder = MaterialAlertDialogBuilder(this@MainActivity)
                builder.setTitle("提示")
                builder.setPositiveButton("复制") { d, w ->
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("", copyText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this@MainActivity, "已复制", Toast.LENGTH_SHORT).show()

                }
                // 处理菜单项点击
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    val title = menuItem.title
                    when (title) {
                        "导入项目" -> {

                        }

                        "导出为XML" -> {
                            copyText = File(iconPath).readText()
                            builder.setMessage(copyText)

                            builder.show()
                        }

                        "导出为SVG" -> {
                            val converter = Vd2SvgConverter()
                            copyText = converter.convert(File(iconPath))
                            builder.setMessage(copyText)
                            builder.show()
                        }

                        "导出为PNG" -> {}
                        "编辑" -> {

                        }
                    }
                    true
                }
                popupMenu.show()
            }
        }

        override fun getItemCount() = items.size

        fun updateItems(items: List<Icon>) {
            this.items.clear()
            this.items.addAll(items)
            notifyDataSetChanged()
        }


    }

    class ViewHolder(val binding: ItemIconBinding) : RecyclerView.ViewHolder(binding.root)

    data class IconMenu(val id: Int, val title: String, val icon: Int)
}

fun readString(file: File) = file.readText(charset = StandardCharsets.UTF_8)


data class IconsMetadata(
    val families: List<String>, val categories: MutableList<String>, val icons: MutableList<Icon>
)


data class Icon(
    val name: String,
    @SerializedName("unsupported_families") val unsupportedFamilies: List<String>,
    val categories: List<String>,
    val tags: List<String>
) {

    fun getIconPath(storeDir: File, family: String): String {
        val prefix = when {
            family == "Material Icons" -> "baseline"
            family.endsWith("Outlined") -> "outline"
            else -> family.replace("Material Icons", "").replace(" ", "").trim().lowercase()
        }

        val familyDirName = family.replace(" ", "").lowercase()
        val fileName = "${prefix}_${name}_24.xml"

        return File(
            storeDir, "$familyDirName/$name/$fileName"
        ).absolutePath
    }
}
