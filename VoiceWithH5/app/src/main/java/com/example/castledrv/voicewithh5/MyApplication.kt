package com.example.castledrv.voicewithh5

import android.app.Application
import android.content.Context
import com.apkfuns.logutils.LogLevel
import com.apkfuns.logutils.LogUtils
import kotlin.properties.Delegates

/**
 * Created by CastleDrv on 2017/7/16.
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        LogUtils.getLogConfig()
                .configAllowLog(true)
                .configTagPrefix("MyAppName")
                .configShowBorders(true)
                .configFormatTag("%d{HH:mm:ss:SSS} %t %c{-5}")
                .configLevel(LogLevel.TYPE_VERBOSE)
    }

    companion object {
        var instance: MyApplication by Delegates.notNull()
    }


}