/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin

import net.rwhps.server.data.global.Data
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess

/**
 * 加载 JavaScript 脚本并注入依赖
 *
 * @author RW-HPS/Dr
 */
object JavaScriptPlugin {
    private val lib = """
    """.trimIndent()

    fun loadJavaScriptPlugin(script: String): Plugin {
        val context = Context.newBuilder()
            .option("engine.WarnInterpreterOnly", "false")
            .allowHostAccess(HostAccess.newBuilder()
                .allowAllClassImplementations(true)
                .allowAllImplementations(true)
                .build())
            .allowHostClassLookup { _ -> true }
            .build()
        val plugin = context.use {
            context.eval("js", "$lib${Data.LINE_SEPARATOR}$script")
            context.getBindings("js").getMember("main").execute().`as`(Plugin::class.java)
        }
        return plugin
    }
}
