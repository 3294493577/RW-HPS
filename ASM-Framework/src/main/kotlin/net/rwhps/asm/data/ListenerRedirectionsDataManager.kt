/*
 *
 *  * Copyright 2020-2023 RW-HPS Team and contributors.
 *  *
 *  * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  *
 *  * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 *
 */

package net.rwhps.asm.data

import net.rwhps.asm.api.listener.RedirectionListener

/**
 *
 *
 * @date 2023/10/22 9:48
 * @author Dr (dr@der.kim)
 */
object ListenerRedirectionsDataManager {
    /** Partial Class Path缓存 建少查询 */
    internal val partialClassPathCache = ArrayList<String>()
    /** 替代指定方法 */
    internal val partialListenerMethodName = HashMap<String, ArrayList<MethodTypeInfoValue>>()

    internal val descData = HashMap<String, RedirectionListener>()

    /**
     * 加入 指定 Class 的指定方法代理
     *
     *
     * @param desc String
     * @param p Array<String>
     * @param redirection Redirection
     */
    @JvmStatic
    fun addPartialMethodListener(methodTypeInfoValue: MethodTypeInfoValue, redirection: RedirectionListener?) {
        if (methodTypeInfoValue.listenerClass == null && redirection == null) {
            throw NullPointerException("Parameter error")
        }

        if (methodTypeInfoValue.replaceClass != null) {
            throw NullPointerException("It should not be passed in : ReplaceClass")
        }

        if (partialListenerMethodName.containsKey(methodTypeInfoValue.classPath)) {
            val list = partialListenerMethodName[methodTypeInfoValue.classPath]!!
            if (list.contains(methodTypeInfoValue)) {
                throw Exception("Repeat")
            }
            list.add(methodTypeInfoValue)
        } else {
            partialListenerMethodName[methodTypeInfoValue.classPath] = ArrayList<MethodTypeInfoValue>().also { it.add(methodTypeInfoValue) }
        }

        // 如果 ReplaceClass 为 null, 那么即证明是默认替换, 才需要加入redirection
        if (methodTypeInfoValue.listenerClass == null) {
            // 这里构造 Desc 并且 加入对应的 取代
            descData[methodTypeInfoValue.desc] = redirection!!
        }
        addPartialClassPathCache(methodTypeInfoValue.classPath)
    }

    private fun addPartialClassPathCache(classPath: String) {
        partialClassPathCache.add(classPath)
    }
}