package io.github.yzjdev.mdicon

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.tencent.mmkv.MMKV

fun Any?.log(tag: String = "aaa") {
	Log.d(tag, "$this")
}

fun Any.toast(context: Context) {
	Handler(Looper.getMainLooper()).post {
		Toast.makeText(context, "$this", Toast.LENGTH_SHORT).show()
	}
}

val mmkv = MMKV.defaultMMKV()