package io.github.duzhaokun123.darkaischedule

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.RadioButton
import android.widget.Toast
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookAllConstructorAfter
import com.github.kyuubiran.ezxhelper.utils.loadClass
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XposedInit : IXposedHookLoadPackage {
    companion object {
        val TAG = "DarkAISchedule"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.xiaomi.aischedule") return
        EzXHelperInit.initHandleLoadPackage(lpparam)
        WebView::class.java.hookAllConstructorAfter {
            val thiz = it.thisObject as WebView
            thiz.settings.javaScriptEnabled = true
            thiz.addJavascriptInterface(Xiaoai(), "xiaoai")
            thiz.loadUrl("javascript:window.xiaoai = xiaoai")
            thiz.addJavascriptInterface(XiaoaiDevice(), "xiaoai_device")
            thiz.loadUrl("javascript:window.xiaoai_device = xiaoai_device")
        }
        loadClass("com.xiaomi.aischedule.activity.MainActivity").findMethod {
            name == "onCreate" && parameterTypes.getOrNull(0) == Bundle::class.java
        }.hookAfter {
            val thiz = it.thisObject as Activity
            thiz.findViewById<RadioButton>(thiz.getResId("tv_tab_today", "id")).visibility = View.GONE
            thiz.findViewById<RadioButton>(thiz.getResId("tv_tab_schedule", "id")).visibility = View.GONE
            thiz.findViewById<RadioButton>(thiz.getResId("tv_tab_my", "id")).visibility = View.GONE
        }
    }

    class Xiaoai {
        /**
         * ```js
         * window.xiaoai.getAppVersion("com.miui.voiceassist") >= 305017e3
         * ```
         */
        @JavascriptInterface
        fun getAppVersion(packageName: String): String {
            Log.d(TAG, "getAppVersion: $packageName")
            return if (packageName == "com.miui.voiceassist") "305017e4" else "0"
        }

        /**
         * ```js
         * window.xiaoai.getUserInfo();
         * ```
         */
        @JavascriptInterface
        fun getUserInfo() {
            Log.d(TAG, "getUserInfo: here")
        }
    }

    class XiaoaiDevice {
        /**
         * ```js
         * window.xiaoai_device.isDarkMode()
         * ```
         */
        @JavascriptInterface
        fun isDarkMode(): Int {
            return 1
        }

        /**
         * ```js
         * window.xiaoai_device.engine("getSystemModel", "", [r, a, ""])
         * ```
         */
        @JavascriptInterface
        fun engine(a: String, b: String, c: Array<String>) {
            Log.d(TAG, "engine: $a $b ${c.joinToString()}")
        }
    }

    private fun Activity.getResId(name: String, type: String): Int {
        return resources.getIdentifier(name, type, packageName)
    }
}