package com.example.castledrv.voicewithh5.utils


import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

/**
 * 通用工具类

 * @author xiezw
 */
object CommonUtil {
    internal var TAG = "CommonUtil"
    private val SeparatoChar_hour = "时"
    private val SeparatoChar_min = "分"
    private val SeparatoChar_sec = "秒"

    private val ret = ""
    private val now = Calendar.getInstance()
    private val ms_today = (1000 * (now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60 + now
            .get(Calendar.SECOND))).toLong()// 毫秒数
    private val ms_yesterday = ms_today + 24 * 3600 * 1000
    private val ms_before_yesterday = ms_today + 24 * 3600 * 1000 * 2
    private val ms_now = now.timeInMillis
    private val sdf_hhmm = SimpleDateFormat("HH:mm")
    private val sdf_hhmmss = SimpleDateFormat("HH:mm:ss")
    private val sdf_yyMMdd = SimpleDateFormat("yyyy.MM.dd")
    private val myFmt = SimpleDateFormat("yyyy年MM月dd日 HH:mm")
    private val sdf_hms = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")
    private val decimalFormat = DecimalFormat("##00")
    private val sdf_yyyyMMdd = SimpleDateFormat("yyyy年MM月dd日")

    private val sdf_date = SimpleDateFormat("yyyy.MM.dd")
    private val otherChatSdf = SimpleDateFormat("M月d日")

    fun getFormatTime(time: Long): String {
        val date = Date(time)
        val formatTime = sdf_hhmm.format(date)
        return formatTime
    }

    fun getFormatDate(time: Long): String {
        val date = Date(time)
        val formatTime = sdf_date.format(date)
        return formatTime
    }

    private fun getWeekOfDate(date: Date): String {
        val weekDays = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
        val cal = Calendar.getInstance()
        cal.time = date
        var w = cal.get(Calendar.DAY_OF_WEEK) - 1
        if (w < 0)
            w = 0

        return weekDays[w]
    }

    fun formatCallingTimeStr(time: Long?): String {
        var hour = 0
        var minute = 0
        var second = 0
        if (time == 0L)
            return "00:00"
        second = time!!.toInt()

        if (second >= 60) {
            minute = second / 60
            second %= 60
        }
        if (minute >= 60) {
            hour = minute / 60
            minute %= 60
        }
        if (hour != 0) {
            return hour.toString() + ":" + decimalFormat.format(minute.toLong()) + ":" + decimalFormat.format(second.toLong())
        } else if (hour == 0 && minute != 0) {
            return decimalFormat.format(minute.toLong()) + ":" + decimalFormat.format(second.toLong())
        } else {
            return "00:" + decimalFormat.format(second.toLong())
        }
    }
}