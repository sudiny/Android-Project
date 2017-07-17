package com.example.castledrv.voicewithh5.utils

import android.os.Build
import android.os.Environment
import android.os.StatFs

import java.io.File

object SdCardUtils {

    /**
     * 返回SD卡跟目录
     */
    //判断sd卡是否存在
    //获取跟目录
    val sdCardPath: String?
        get() {
            var sdDir: File? = null
            val sdCardExist = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
            if (sdCardExist) {
                sdDir = Environment.getExternalStorageDirectory()
                return sdDir!!.toString()
            } else
                return null
        }

    /**
     * 判断SD卡是否存在
     */
    val isSdCardExist: Boolean
        get() {
            return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        }

    /**
     * 获取SD卡的剩余容量

     * @return
     */
    //            totalBlocks = stat.getBlockCountLong();
    //            totalBlocks = stat.getBlockCount();
    val availableLong: Long
        get() {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            var availableBlocks: Long = 0
            var blockSize: Long = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.blockSizeLong
                availableBlocks = stat.availableBlocksLong
            } else {
                blockSize = stat.blockSize.toLong()
                availableBlocks = stat.availableBlocks.toLong()
            }
            return availableBlocks * blockSize
        }

    /**
     * SDK容量是否大于10Mb

     * @return
     */
    val isAvailable: Boolean
        get() {
            val available = availableLong
            if (available > 1024 * 1024 * 10) {
                return true
            }
            return false
        }
}