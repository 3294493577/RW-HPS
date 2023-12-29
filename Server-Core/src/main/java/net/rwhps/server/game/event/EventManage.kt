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

package net.rwhps.server.game.event

import net.rwhps.server.game.event.core.AbstractEvent
import net.rwhps.server.util.concurrent.AbstractFuture
import java.util.function.Consumer

/**
 * 在 Hess 上的 Event 管理器实现
 *
 * @date 2023/7/5 10:00
 * @author Dr (dr@der.kim)
 */
@Suppress("EXPOSED_SUPER_CLASS")
open class EventManage: AbstractEventManage(AbstractEvent::class.java) {
    /**
     * 执行新事件
     *
     * @param type 全局事件
     */
    fun fire(type: AbstractEvent): AbstractFuture<*> = fire0(type)

    open fun <T: AbstractEvent> registerListener(eventClass: Class<T>, consumer: Consumer<T>) {
        eventData.addEvent(eventClass) { value ->
            consumer.accept(value)
        }
    }
}