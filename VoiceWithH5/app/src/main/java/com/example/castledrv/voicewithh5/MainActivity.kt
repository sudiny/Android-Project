package com.example.castledrv.voicewithh5

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import android.view.ViewGroup
import android.webkit.*
import android.widget.LinearLayout
import com.apkfuns.logutils.LogUtils
import com.example.castledrv.voicewithh5.R.id.ll_container
import com.google.gson.Gson
import me.weyye.hipermission.HiPermission
import me.weyye.hipermission.PermissionCallback
import me.weyye.hipermission.PermissionItem
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.util.ArrayList


class MainActivity : AppCompatActivity() {


    private lateinit var mWebView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initData()
        initWebSetting()
        mWebView.loadUrl("file:///android_asset/myWork/index.html")
        initWebClient()
        initChromeClient()
    }

    val permissionItems = ArrayList<PermissionItem>()
    private fun initData() {
        permissionItems.add(PermissionItem(Manifest.permission.RECORD_AUDIO, "麦克风", R.drawable.permission_ic_micro_phone))
        permissionItems.add(PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储", R.drawable.permission_ic_storage))
    }

    private fun initWebClient() {
        mWebView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                val uri: Uri = Uri.parse(url)
                if (uri.scheme == "js") {
                    if (uri.authority == "webview") {
                        //在这里执行录音
                        HiPermission.create(view!!.context)
                                .permissions(permissionItems)
                                .checkMutiPermission(object : PermissionCallback {
                                    override fun onGuarantee(p0: String?, p1: Int) {
                                    }

                                    override fun onFinish() {
                                        startActivityForResult(Intent(view.context, NewRecordTimesActivity::class.java), 100)
                                    }

                                    override fun onDeny(p0: String?, p1: Int) {
                                    }

                                    override fun onClose() {
                                        toast("缺少必要权限...")
                                    }
                                })
                    }
                    return true
                }
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                val allTime = data!!.getStringExtra("time")
                val startTime = data.getStringExtra("startTime")
                val path = data.getStringExtra("FILE PATH")
                val gson = JSONObject()
                gson.put("all_time", allTime)
                gson.put("start_time", startTime)
                gson.put("src", path)
                mWebView.loadUrl("javascript:returnResult('" + gson + "')")

            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebSetting() {
        val webSettings = mWebView.settings
        with(webSettings) {
            javaScriptEnabled = true
            useWideViewPort = true
            allowFileAccess = true
            loadsImagesAutomatically = true
            javaScriptCanOpenWindowsAutomatically = true
            databaseEnabled = true
            domStorageEnabled = true
        }
    }

    fun initChromeClient() {
        mWebView.setWebChromeClient(object : WebChromeClient() {
            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                return super.onJsAlert(view, url, message, result)
            }
        })
    }

    private fun initView() {
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mWebView = WebView(applicationContext)
        mWebView.layoutParams = params
        ll_container.addView(mWebView)
    }

    override fun onDestroy() {
        mWebView.clearHistory()
        (mWebView.parent as ViewGroup).removeView(mWebView)
        mWebView.destroy()
        super.onDestroy()
    }
}
