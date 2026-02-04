package io.github.yzjdev.mdicon


import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import io.github.yzjdev.mdicon.adapter.IconFontAdapter
import io.github.yzjdev.mdicon.databinding.ActivityIconfontBinding
import io.github.yzjdev.mdicon.exts.loadSvgFromString
import io.github.yzjdev.mdicon.model.IconFont
import io.github.yzjdev.mdicon.model.IconItem
import io.github.yzjdev.mdicon.model.SearchResult
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x

class IconFontActivity : AppCompatActivity() {
	val ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8113.400"
	val TAG = "aaa"

	private lateinit var binding: ActivityIconfontBinding

	lateinit var iconFont: IconFont

	lateinit var iconFontAdapter: IconFontAdapter
	lateinit var searchView: SearchView

	val allIcons = mutableListOf<IconItem>()
	val displayIcons = mutableListOf<IconItem>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		binding = ActivityIconfontBinding.inflate(layoutInflater)
		setContentView(binding.root)
		ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}

		setSupportActionBar(binding.toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		init()

		try {
			val str = mmkv.getString("local_icons", "")
			val gson = Gson()
			iconFont = gson.fromJson(str, IconFont::class.java)
			setupRecyclerView()
		} catch (e: Exception) {
			loadOnline()
		}

	}


	fun init() {
		binding.rv.layoutManager = GridLayoutManager(this, 4)
	}


	fun setupRecyclerView() {
		allIcons.clear()
		iconFont.data.collections.forEach {
			allIcons.addAll(it.icons)
		}
		displayIcons.clear()
		displayIcons.addAll(allIcons)
		iconFontAdapter = IconFontAdapter(this, displayIcons) { b, item, position ->
			b.icon.loadSvgFromString(item.svgString)
			b.name.text = item.name
		}
		binding.rv.adapter = iconFontAdapter
	}

	fun loadOnline() {
		loadOnline(1)
	}

	fun loadOnline(page: Int) {
		val url = "https://www.iconfont.cn/api/collections.json?type=3&sort=time&limit=9&page=$page&keyword=&t=1770164280710&ctoken=null"
		val params = RequestParams(url).apply {
			setHeader("User-Agent", ua)
		}
		x.http().get(params, object : HttpCallback<String>() {
			override fun onSuccess(result: String?) {
				result?.let {
					val gson = Gson()
					iconFont = gson.fromJson(it, IconFont::class.java)
					val str = gson.toJson(iconFont)
					mmkv.putString("local_icons", str)
					setupRecyclerView()
				}
			}
		})
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.iconfont_menu, menu)
		val searchItem = menu?.findItem(R.id.search)
		searchView = searchItem?.actionView as SearchView
		searchView.isSubmitButtonEnabled = true
		searchView.queryHint = "搜索图标..."
		searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(query: String?): Boolean {
				query?.let { search(it) }
				return false
			}

			override fun onQueryTextChange(newText: String?): Boolean {
				if (newText.isNullOrBlank()) {
					iconFontAdapter.updateItems(allIcons)
				}
				return false
			}
		})
		return super.onCreateOptionsMenu(menu)
	}


	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			android.R.id.home -> finish()
			R.id.refresh -> loadOnline()
		}
		return true
	}

	fun search(key: String) {
		val params = RequestParams("https://www.iconfont.cn/api/icon/search.json").apply {
			setHeader("User-Agent", ua)
			addBodyParameter("q", key)
			addBodyParameter("sortType", "updated_at")
			addBodyParameter("page", "1")
			addBodyParameter("pageSize", "54")
			addBodyParameter("sType", "")
			addBodyParameter("fromCollection", "-1")
			addBodyParameter("fills", "")
			addBodyParameter("t", System.currentTimeMillis())
			addBodyParameter("ctoken", "null")
		}
		x.http().post(params, object : HttpCallback<String>() {
			override fun onSuccess(result: String?) {
				result.log()
				result?.let {
					val gson = Gson()
					val searchResult = gson.fromJson(it, SearchResult::class.java)
					iconFontAdapter.updateItems(searchResult.data.icons)
				}
			}
		})
	}

	abstract class HttpCallback<T> : Callback.CommonCallback<T> {

		override fun onError(ex: Throwable?, isOnCallback: Boolean) {

		}

		override fun onCancelled(cex: Callback.CancelledException?) {

		}

		override fun onFinished() {

		}
	}
}