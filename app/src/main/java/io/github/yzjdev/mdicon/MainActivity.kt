package io.github.yzjdev.mdicon

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.ViewCompat.setLayerType
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.caverock.androidsvg.SVG
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import io.github.yzjdev.mdicon.databinding.ActivityMainBinding
import io.github.yzjdev.mdicon.databinding.ItemIconBinding
import io.github.yzjdev.mdicon.databinding.TestBinding
import io.github.yzjdev.svg2vector.Vector2Svg

import net.lingala.zip4j.ZipFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.atomics.ExperimentalAtomicApi

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

    val searchExecutor = Executors.newSingleThreadExecutor()
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
        XXPermissions.with(this).permission(PermissionLists.getManageExternalStoragePermission())
            .request(null)
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
        val json = readString(iconsMetadataFile)
        iconsMetadata = gson.fromJson(json, IconsMetadata::class.java)
        filteredIcons = ArrayList(iconsMetadata.icons)
        setupSpinner()
        setupSpinner2()


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
                binding.toolbar.subtitle = "${list.size}"
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
            R.id.action_help -> {
                val file = File(
                    getExternalFilesDir(null),
                    "icons/material/materialiconstwotone/10k/twotone_10k_24.xml"
                )

                val testFilePath = "/storage/emulated/0/Download/test.xml"
                val b = TestBinding.inflate(LayoutInflater.from(this), null, false)
                b.icon.loadVectorDrawable(File(testFilePath))
                MaterialAlertDialogBuilder(this).apply {
                    setView(b.root)
                    show()
                }
            }
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
            val file = File(iconPath)
            holder.binding.name.text = item.name
            holder.binding.icon.loadVectorDrawable(file)

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
                            file.copyTo(File(getExternalFilesDir(null), file.name), true)
                        }

                        "导出为SVG" -> {
                            val str = Vector2Svg.parseXmlToSvg(FileInputStream(file))
                            str.log()
//                           Vector2Svg.parseXmlToSvg(
//                                FileInputStream(file), FileOutputStream(
//                                    File(
//                                        getExternalFilesDir(null), "${item.name}.svg"
//                                    )
//                                )
//                            )
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
