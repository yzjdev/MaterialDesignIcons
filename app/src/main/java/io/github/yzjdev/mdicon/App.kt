package io.github.yzjdev.mdicon

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.tencent.mmkv.MMKV
import org.xutils.x

class App : Application() {
	companion object {
		lateinit var instance: App
	}

	override fun onCreate() {
		super.onCreate()
		instance = this
		DynamicColors.applyToActivitiesIfAvailable(this)
		MMKV.initialize(this)
		x.Ext.init(this)
	}
}
