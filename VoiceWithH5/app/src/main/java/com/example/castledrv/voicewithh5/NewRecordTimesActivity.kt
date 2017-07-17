package com.example.castledrv.voicewithh5

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.apkfuns.logutils.LogUtils
import com.example.castledrv.voicewithh5.service.FileUploadService
import com.example.castledrv.voicewithh5.utils.Base64
import com.example.castledrv.voicewithh5.utils.CommonUtil
import com.example.castledrv.voicewithh5.utils.FileHelper
import com.example.castledrv.voicewithh5.utils.SdCardUtils
import com.example.castledrv.voicewithh5.widget.WaveView
import com.vongvia.kotlingank.net.NetWork

import kotlinx.android.synthetic.main.activity_new_audiorecord.*
import me.weyye.hipermission.HiPermission
import me.weyye.hipermission.PermissionCallback
import me.weyye.hipermission.PermissionItem
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.jetbrains.anko.toast
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by CastleDrv on 2017/7/16.
 */
class NewRecordTimesActivity : AppCompatActivity() {
    companion object {
        val maxValue = 8
        val SUFFIX = ".amr"
        val mMaxVolume = 2 shl 14 - 1
        val STATE_DEFAULT = 0
        val STATE_RECORDING = 1
        val STATE_READY_TO_PLAY = 2
        val STATE_PLAYING = 3
        val DEFAULT_TIMES = "00:00"
        val NAME_FILE_PATH = "FILE PATH"
    }

    private var state_current = 0
    private lateinit var mBtnStart: ImageView
    private lateinit var mBtnStop: ImageView
    private lateinit var mBtnPlay: ImageView
    private lateinit var mTvCancel: TextView
    private lateinit var mTvFinish: TextView
    private lateinit var mTv_noterecord: TextView
    private lateinit var mTvTitle: TextView
    private lateinit var mWaveView_left: WaveView
    private lateinit var mWaveView_right: WaveView


    /**
     * 录音保存路径*
     */
    private lateinit var myRecAudioDir: File
    private var myRecAudioFile: File? = null
    private var mMediaRecorder: MediaRecorder? = null
    private val mMinute: Int = 0
    /**
     * 存放音频文件列表*
     */
    private lateinit var myTextView1: TextView
    /**
     * 文件存在*
     */
    private var sdcardExit: Boolean = false
    /**
     * 是否停止录音*
     */
    private var isStopRecord: Boolean = false


    /**
     * 计时器*
     */
    internal var timer: Timer? = null

    /**
     * 计时器*
     */
    internal var timer_play: Timer? = null

    private var mStartTime: Long = 0
    private var mediaPlayer: MediaPlayer? = null
    private var totalTimes = 0
    internal var isPlaying = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_audiorecord)
        initView()
        initData()
    }

    private fun initData() {
        permissionItems.add(PermissionItem(Manifest.permission.RECORD_AUDIO, "麦克风", R.drawable.permission_ic_micro_phone))
        permissionItems.add(PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储", R.drawable.permission_ic_storage))
        // 判断sd Card是否插入
        sdcardExit = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        // 取得sd card路径作为录音文件的位置
        if (sdcardExit) {
            var pathStr: String? = null
            try {
                pathStr = FileHelper.getBasePath("audio").toString()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            myRecAudioDir = File(pathStr!!)
            if (!myRecAudioDir.exists()) {
                myRecAudioDir.mkdirs()
            }
        }
        // 录音
        initEvent()
        changeUI(STATE_DEFAULT)
    }

    val permissionItems = ArrayList<PermissionItem>()
    private fun initEvent() {
        mBtnStart.setOnTouchListener (object:View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event!!.action) {
                    MotionEvent.ACTION_DOWN -> {
                        HiPermission.create(v!!.context)
                                .permissions(permissionItems)
                                .checkMutiPermission(object : PermissionCallback {
                                    override fun onFinish() {
                                        startRecord()
                                    }

                                    override fun onDeny(p0: String?, p1: Int) {
                                        toast("缺少必要权限将无法执行录音功能...")
                                    }

                                    override fun onGuarantee(p0: String?, p1: Int) {
                                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    }

                                    override fun onClose() {
                                        toast("缺少必要权限...")
                                    }
                                })
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isPlaying) {
                            stopPlayRecord()
                        } else {
                            stopRecord()
                        }
                    }
                }
                return true
            }
        })
//        mBtnStart.setOnClickListener({
//
//        })
        // 停止
//        mBtnStop.setOnClickListener({
//            if (isPlaying) {
//                stopPlayRecord()
//            } else {
//                stopRecord()
//            }
//        })
        // 播放
        mBtnPlay.setOnClickListener({ playRecord() })
        mTvCancel.setOnClickListener({ cancelAudioRecord() })
        mTvFinish.setOnClickListener({ finishAudioRecord() })

    }

    private fun playRecord() {
        mediaPlayer = null
        if (myRecAudioFile != null) {
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.reset()
            try {
                mediaPlayer!!.setDataSource(myRecAudioFile!!.absolutePath)
            } catch (e: IOException) {
                e.printStackTrace()
                return
            }

        }

        mediaPlayer!!.setOnCompletionListener({ mediaPlayer ->
            if (timer_play != null) {
                timer_play!!.cancel()
                timer_play = null
            }
            isPlaying = false
            changeUI(STATE_READY_TO_PLAY)
            handler!!.removeCallbacksAndMessages(null)
            if (mediaPlayer != null) {
                mediaPlayer.stop()
                mediaPlayer.release()
            }
        })
        try {
            mediaPlayer!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }

        totalTimes = mediaPlayer!!.duration
        mediaPlayer!!.start()
        val timerTask = object : TimerTask() {

            override fun run() {
                if (handler != null && mediaPlayer != null) {
                    val msg = handler!!.obtainMessage()
                    val s = (totalTimes - mediaPlayer!!.currentPosition) / 1000
                    msg.obj = CommonUtil.formatCallingTimeStr(java.lang.Long.valueOf(s.toLong()))
                    handler!!.sendMessage(msg)
                }
            }
        }
        timer_play = Timer()
        timer_play!!.schedule(timerTask, 1000, 1000)
        isPlaying = true
        changeUI(STATE_PLAYING)
        mAudioManager = MyApplication.instance.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager.mode = AudioManager.MODE_NORMAL
    }

    lateinit var mAudioManager: AudioManager
    private fun noHasRecordAudioPermission() {
        toast("没有录音权限，请检查设置")
    }

    private fun stopPlayRecord() {
        isPlaying = false
        timer_play!!.cancel()
        if (mediaPlayer != null) {
            mediaPlayer!!.setOnCompletionListener(null)
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
        changeUI(STATE_READY_TO_PLAY)
    }

    //取消录音
    private fun cancelAudioRecord() {
        when (state_current) {
            STATE_DEFAULT -> {
            }
            STATE_READY_TO_PLAY -> {
                changeUI(STATE_DEFAULT)
                myRecAudioFile = null
            }
            STATE_RECORDING -> {
                stopRecord()
                changeUI(STATE_DEFAULT)
                myRecAudioFile = null
            }
        }
    }

    private fun finishAudioRecord() {
        setResultBack()
    }

    fun uploadFiles(file: File) {
        //代理模式生成对应server的实例化对象
        var server = NetWork.create(FileUploadService::class.java, "baseUrl")
        var requestBody = RequestBody.create(MediaType.parse("amr/*"), file)
        //使用RxJava方式调度任务并监听
        server.uploadFile(requestBody)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : Subscriber<ResponseBody>() {
                    override fun onCompleted() {
                        LogUtils.d("上传完成")
                    }

                    override fun onError(e: Throwable?) {
                        LogUtils.d("上传失败")
                    }

                    override fun onNext(t: ResponseBody?) {
                        LogUtils.d("上传完毕")
                    }

                })
    }

    private fun setResultBack() {
        val data = Intent()
        var path = ""
        if (myRecAudioFile != null) {
            path = myRecAudioFile!!.absolutePath
        }
        data.putExtra("time", myTextView1.text)
        data.putExtra("startTime", getTime())
        data.putExtra(NewRecordTimesActivity.NAME_FILE_PATH, path)
        setResult(RESULT_OK, data)
        finish()
    }

    /**
     * 开始录音
     */
    private fun startRecord() {
        if (!SdCardUtils.isSdCardExist) {
            toast(getString(R.string.SD_card_not_detected_the_function_of_voice_intercom_temporarily_cannot_use))
            return
        }
        mStartTime = System.currentTimeMillis()
        start()
    }

    /**
     * 停止录音
     */
    private fun stopRecord() {
        try {
            mStartTime = 0
            if (timer != null) {
                timer!!.cancel()
            }
            if (myRecAudioFile != null && mMediaRecorder != null) {
                // 停止录音
                mMediaRecorder!!.stop()
                mMediaRecorder!!.release()
                mMediaRecorder = null
                isStopRecord = true            //停止录音了
                // 将录音频文件给Adapter
                //            DecimalFormat df = new DecimalFormat("#.000");
                if (myRecAudioFile!!.length() == 0L) {   //录音文件大小为0，提示没有录音的权限
                    noHasRecordAudioPermission()
                    changeUI(STATE_DEFAULT)
                    myRecAudioFile = null
                    return
                }
            }
            changeUI(STATE_READY_TO_PLAY)
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (error: Error) {
            error.printStackTrace()
        }

    }

    private fun changeUI(state: Int) {
        state_current = state
        when (state) {
            STATE_DEFAULT -> {
                mBtnStop.visibility = View.INVISIBLE
                mBtnStart.visibility = View.VISIBLE
                mBtnPlay.visibility = View.INVISIBLE
                myTextView1.text = DEFAULT_TIMES
                mTvFinish.visibility = View.INVISIBLE
                mTvCancel.visibility = View.INVISIBLE
                mTv_noterecord.text = "点击录音"
                mTvTitle.text = ""
            }
            STATE_READY_TO_PLAY -> {
                mBtnStop.visibility = View.INVISIBLE
                mBtnStart.visibility = View.INVISIBLE
                mBtnPlay.visibility = View.VISIBLE
                mTvFinish.visibility = View.VISIBLE
                mTvCancel.visibility = View.VISIBLE
                mTv_noterecord.text = ""
                mTvTitle.text = ""
            }
            STATE_RECORDING -> {
                mBtnStart.visibility = View.INVISIBLE
                mBtnStop.visibility = View.VISIBLE
                mBtnPlay.visibility = View.INVISIBLE
                mTvFinish.visibility = View.INVISIBLE
                mTvCancel.visibility = View.INVISIBLE
                mTv_noterecord.text = ""
                mTvTitle.text = "录音中..."
            }
            STATE_PLAYING -> {
                mBtnStart.visibility = View.INVISIBLE
                mBtnStop.visibility = View.VISIBLE
                mBtnPlay.visibility = View.INVISIBLE
                mTvFinish.visibility = View.INVISIBLE
                mTvCancel.visibility = View.INVISIBLE
                mTv_noterecord.text = ""
                mTvTitle.text = "播放中..."
            }
        }
    }

    override fun onStop() {
        if (isPlaying) {
            stopPlayRecord()
        } else {
            stopRecord()
        }
        super.onStop()
    }


    /**
     * 开始录录录音

     * @return
     */
    private fun start(): Boolean {
        try {
            if (!sdcardExit) {
                Toast.makeText(this, "请插入SD card", 1000).show()
                return false
            }
            // 创建音频文件
            myRecAudioFile = File(myRecAudioDir, "androidVoice" + Base64.encode(System.currentTimeMillis().toString() + "") + SUFFIX)
            mMediaRecorder = MediaRecorder()
            // 设置录音为麦克风
            mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            //录音文件保存这里
            mMediaRecorder!!.setOutputFile(myRecAudioFile!!.absolutePath)
            mMediaRecorder!!.prepare()
            mMediaRecorder!!.start()
            mMediaRecorder!!.setOnInfoListener(MediaRecorder.OnInfoListener { mr, what, extra ->
                //                    int a = mr.getMaxAmplitude();
                //                    Toast.makeText(NewRecordTimesActivity.this, a + "============", Toast.LENGTH_LONG).show();
            })
            isStopRecord = false
            val timerTask = object : TimerTask() {

                override fun run() {
                    val msg = handler!!.obtainMessage()
                    msg.obj = CommonUtil.formatCallingTimeStr((System.currentTimeMillis() - mStartTime) / 1000)
                    msg.arg1 = mMediaRecorder!!.maxAmplitude
                    handler!!.sendMessage(msg)
                }
            }
            timer = Timer()
            timer!!.schedule(timerTask, 300, 280)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } catch (e: Exception) {
            return false
        }

        changeUI(STATE_RECORDING)
        return true
    }

    private fun getTime(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val curDate = Date(System.currentTimeMillis())//获取当前时间
        val time = formatter.format(curDate)
        return time
    }

    internal var handler: Handler? = object : Handler() {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val s = msg.obj as String
            myTextView1.text = s
            val arg = msg.arg1
            if (arg > 0) {
                updateWave(arg, maxValue)
            }
        }
    }

    internal var list_data: MutableList<Int> = ArrayList()

    internal var number = 0
    private var mMinVolume = 0.0
    internal var mVolume = 0


    /**
     * 计算要绘制的音量

     * @param volume 不限大小
     */
    private fun getVolume(volume: Double, maxValue: Int): Int {
        // 低于最小值设该值为最小值
        if (volume < mMinVolume)
            mMinVolume = volume
        // 如果最大值比最小值大时
        if (mMaxVolume - mMinVolume > 0) {
            // 当前值始终在0~1之间
            mVolume = ((volume - mMinVolume) * 11 / (mMaxVolume - mMinVolume)).toInt()
        } else {
            mVolume = maxValue - 1
        }
        if (mVolume >= maxValue) mVolume = maxValue - 1
        if (mVolume < 0) mVolume = 0
        // 请求刷新视图
        return mVolume
    }

    private fun updateWave(ap: Int, maxValue: Int) {
        val value = getVolume(ap.toDouble(), maxValue)
        if (number == 0 && mWaveView_left.mSpectrumNum > 0) {
            for (i in 0..mWaveView_left.mSpectrumNum - 1) {
                list_data.add(1)
            }
        }
        number = mWaveView_left.mSpectrumNum
        if (number == 0) {
            return
        }
        list_data.add(value)
        if (list_data.size > number) {
            list_data.removeAt(0)
        }
        mWaveView_left.updateData(list_data, maxValue)
        mWaveView_right.updateData(list_data, maxValue)
    }

    private fun initView() {
        mBtnStart = findViewById(R.id.newAudioRecord_ibtn_start) as ImageView
        mBtnStop = findViewById(R.id.newAudioRecord_ibtn_stop) as ImageView
        mBtnPlay = findViewById(R.id.newAudioRecord_ibtn_play) as ImageView
        myTextView1 = findViewById(R.id.TextView01) as TextView
        mTvCancel = findViewById(R.id.newAudioRecord_tv_cancel) as TextView
        mTvFinish = findViewById(R.id.newAudioRecord_tv_finish) as TextView
        mTv_noterecord = findViewById(R.id.newAudioRecord_tv_noterecord) as TextView
        mTvTitle = findViewById(R.id.newAudioRecord_tv_title) as TextView
        mWaveView_left = findViewById(R.id.newAudioRecord_waveview_left) as WaveView
        mWaveView_right = findViewById(R.id.newAudioRecord_waveview_right) as WaveView
        mWaveView_right.setIsRight(false)
    }
}