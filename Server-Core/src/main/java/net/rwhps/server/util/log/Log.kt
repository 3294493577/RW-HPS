/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util.log

import net.rwhps.server.data.global.Data
import net.rwhps.server.data.plugin.PluginManage
import net.rwhps.server.game.event.global.ServerConsolePrintEvent
import net.rwhps.server.util.Time.getMilliFormat
import net.rwhps.server.util.file.FileUtils
import net.rwhps.server.util.inline.ifNullResult
import net.rwhps.server.util.log.ColorCodes.formatColors
import net.rwhps.server.util.log.exp.ExceptionX
import java.io.FileOutputStream
import java.text.MessageFormat

/**
 * Log Util
 *
 * 在这里实现 RW-HPS 的主要输出
 *
 * 如果您需要使用 [println] 等接口, 那么请使用 [Data.privateOut] , [System.out] 被 `RW-HPS` 替换
 *
 * 替换后只检测 `/n` `/r/n` 来输出
 *
 * @date 2020年3月8日星期日 3:54
 * @author RW-HPS/Dr
 * @version 1.1
 */
@Suppress("UNUSED")
object Log {
    /** 默认 WARN  */
    private var LOG_GRADE = 5
    private val outLog: FileOutputStream

    /** 默认错误捕获器 */
    val errorDispose = Thread.UncaughtExceptionHandler { thread, error ->
        if (thread != null) {
            error("[Error] ${thread.name}", error)
        } else {
            error("[Error] ?Thread", error)
        }
    }

    init {
        val logLogFile = FileUtils.getFolder(Data.Plugin_Log_Path).toFile("Log.txt")
        outLog = logLogFile.writeByteOutputStream(logLogFile.file.length() <= 512 * 1024)

        // 设置默认的线程异常捕获处理器
        Thread.setDefaultUncaughtExceptionHandler(errorDispose)

        // 设置主线程名字和捕获器
        Thread.currentThread().name = "Main"
        Thread.currentThread().uncaughtExceptionHandler = errorDispose
    }

    /**
     * 设置 [Log] 级别
     *
     * @param log [Logg]
     */
    @JvmStatic
    fun set(log: String) {
        LOG_GRADE = Logg.from(log).logg
    }

    /**
     * Log：
     * tag 标题 默认警告级
     */
    @JvmStatic
    fun skipping(e: Throwable) {
        log(9, "SKIPPING", e)
    }

    @JvmStatic
    fun skipping(tag: Any, e: Throwable) {
        log(9, tag, e)
    }

    @JvmStatic
    fun skipping(e: Any) {
        logs(9, "SKIPPING", e)
    }

    @JvmStatic
    fun skipping(tag: Any, e: Any) {
        logs(9, tag, e)
    }

    @JvmStatic
    fun fatal(e: Throwable) {
        log(7, "FATAL", e)
    }

    @JvmStatic
    fun fatal(tag: Any, e: Throwable) {
        log(7, tag, e)
    }

    @JvmStatic
    fun fatal(e: Any) {
        logs(7, "FATAL", e)
    }

    @JvmStatic
    fun fatal(tag: Any, e: Any) {
        logs(7, tag, e)
    }

    @JvmStatic
    fun error(e: Throwable) {
        log(6, "ERROR", e)
    }

    @JvmStatic
    fun error(tag: Any, e: Throwable) {
        log(6, tag, e)
    }

    @JvmStatic
    fun error(e: Any) {
        logs(6, "ERROR", e)
    }

    @JvmStatic
    fun error(tag: Any, e: Any) {
        logs(6, tag, e)
    }

    @JvmStatic
    fun warn(e: Throwable) {
        log(5, "WARN", e)
    }

    @JvmStatic
    fun warn(tag: Any, e: Throwable) {
        log(5, tag, e)
    }

    @JvmStatic
    fun warn(e: Any) {
        logs(5, "WARN", e)
    }

    @JvmStatic
    fun warn(tag: Any, e: Any) {
        logs(5, tag, e)
    }

    @JvmStatic
    fun info(e: Throwable) {
        log(4, "INFO", e)
    }

    @JvmStatic
    fun info(tag: Any, e: Throwable) {
        log(4, tag, e)
    }

    @JvmStatic
    fun info(e: Any) {
        logs(4, "INFO", e)
    }

    @JvmStatic
    fun info(tag: Any, e: Any) {
        logs(4, tag, e)
    }

    @JvmStatic
    fun debug(e: Throwable) {
        log(3, "DEBUG", e)
    }

    @JvmStatic
    fun debug(tag: Any, e: Throwable) {
        log(3, tag, e)
    }

    @JvmStatic
    fun debug(e: Any) {
        logs(3, "DEBUG", e)
    }

    @JvmStatic
    fun debug(tag: Any, e: Any) {
        logs(3, tag, e)
    }

    @JvmStatic
    fun track(e: Throwable) {
        log(2, "TRACK", e)
    }

    @JvmStatic
    fun track(tag: Any, e: Throwable) {
        log(2, tag, e)
    }

    @JvmStatic
    fun track(e: Any) {
        logs(2, "TRACK", e)
    }

    @JvmStatic
    fun track(tag: Any, e: Any) {
        logs(2, tag, e)
    }


    @JvmStatic
    fun all(e: Throwable) {
        log(1, "ALL", e)
    }

    @JvmStatic
    fun all(tag: Any, e: Throwable) {
        log(1, tag, e)
    }

    @JvmStatic
    fun all(e: Any) {
        logs(1, "ALL", e)
    }

    @JvmStatic
    fun all(tag: Any, e: Any) {
        logs(1, tag, e)
    }

    @JvmStatic
    fun clog(text: String) {
        val textCache = "[" + getMilliFormat(1) + "] " + text
        print(false, formatColors("$textCache&fr"))
    }

    @JvmStatic
    fun clog(text: String, vararg obj: Any?) {
        clog(MessageFormat(text).format(obj))
    }

    @JvmStatic
    fun testPrint(`object`: Any) {
        info(`object`)
    }

    @JvmStatic
    fun saveLog() {
        outLog.flush()
    }

    /**
     * WLog：
     * @param i Warning level -INT
     * @param tag Title / Default warning level-String
     * @param e Exception
     * i>=Set the level to write to the file
     */
    private fun log(i: Int, tag: Any, e: Throwable) {
        logs(i, tag, ExceptionX.resolveTrace(e), true)
    }

    private fun logs(i: Int, tag: Any, e: Any, error: Boolean = false) {
        if (this.LOG_GRADE > i && !error) {
            return
        }

        val sb = StringBuilder()
        val lines = e.toString().split(Data.LINE_SEPARATOR).toTypedArray()
        if (error) {
            val stack = Throwable().stackTrace
            var i1 = 0
            while (i1 < stack.size) {
                val ste = stack[i1]
                val className = ste.className + "." + ste.methodName
                if (!className.contains("net.rwhps.server.util.log.Log")) {
                    sb.append("[").append(ste.fileName).append("] : ").append(ste.methodName).append(" : ").append(ste.lineNumber)
                        .append(Data.LINE_SEPARATOR)
                    break
                }
                i1++
            }
        }

        // [Time] Tag:
        // Info
        sb.append("[").append(getMilliFormat(1)).append("] ").append(tag).append(": ")

        // 避免换行
        if (lines.size > 1) {
            sb.append(Data.LINE_SEPARATOR)
            for (line in lines) {
                sb.append(line).append(Data.LINE_SEPARATOR)
            }
            // 去掉最后的换行
            sb.deleteCharAt(sb.length - 1)
        } else {
            sb.append(e.toString())
        }

        print(error, sb.toString())
    }

    private fun print(error: Boolean, text: String) {
        println(text)

        PluginManage.runGlobalEventManage(ServerConsolePrintEvent(text))

        if (error) {
            // Remove Color
            var textCache = text
            for (i in ColorCodes.VALUES.indices) {
                textCache = textCache.replace(ColorCodes.VALUES[i], "")
            }

            outLog.write("$textCache${Data.LINE_SEPARATOR}".toByteArray())
        }
    }

    private enum class Logg(val logg: Int) {
        /* Log level defaults to WARN *//* ALL during development */
        OFF(8),
        FATAL(7),
        ERROR(6),
        WARN(5),
        INFO(4),
        DEBUG(3),
        TRACK(2),
        ALL(1);

        companion object {
            fun from(type: String): Logg = entries.find { it.name == type || it.name.lowercase() == type.lowercase() }.ifNullResult({
                skipping("Log Level Set Error , In: $type")
                ALL
            }) { it }
        }
    }
}