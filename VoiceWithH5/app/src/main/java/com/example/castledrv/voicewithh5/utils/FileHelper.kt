package com.example.castledrv.voicewithh5.utils

import android.os.Environment
import com.example.castledrv.voicewithh5.MyApplication
import com.example.castledrv.voicewithh5.R
import java.io.File
import java.io.IOException


/**
 * 对SD卡文件的管理
 */
object FileHelper {

    val TYPE_DEFAULT = 0
    val TYPE_DIR = 1
    val TYPE_IMG = 2
    val TYPE_TXT = 3
    val TYPE_MUSIC = 4
    val TYPE_APK = 5
    val TYPE_RAR = 6
    val TYPE_WORD = 7
    val TYPE_VIDEO = 8
    val TYPE_XLS = 9
    val TYPE_PDF = 10
    val TYPE_HTML = 11

    private val TAG = "FileHelper"
    private val DEFAULT_COMPRESS_QUALITY = 100
    private val IMAGEPATH = "/image"
    private val CACHE = "/cache"
    private val VOIPPATH = "/logs/voip"
    private val CHATPATH = "/chat"
    private val TMPATH = "telconference"
    private val RECEIVEFILEPATH = "receive file"
    val WHITEBOARD = "whiteBoard"
    val VIDEO = "video"

    private val ENTPATH = "entfiles"

    private var basePath = MyApplication.instance.resources.getString(R.string.app_name)

    fun setBasePath(path: String?) {
        if (path != null)
            basePath = path + File.separator
    }

    /**
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getBasePath(filepath: String): File {
        var path = basePath
        val basePath: File
        path = path.plus(filepath)

        if (SdCardUtils.isSdCardExist) {
            basePath = File(Environment.getExternalStorageDirectory(), path)
        } else {
            basePath = File("", path)
        }

        if (!basePath.exists()) {
            if (!basePath.mkdirs()) {
                throw IOException(String.format("%s cannot be created!", basePath.toString()))
            }
        }
        if (!basePath.isDirectory) {
            throw IOException(String.format("%s is not a directory!", basePath.toString()))
        }
        return basePath
    }


}