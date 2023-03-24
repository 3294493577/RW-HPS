/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.struct

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectList
import it.unimi.dsi.fastutil.objects.ObjectLists
import net.rwhps.server.data.plugin.AbstractPluginData
import net.rwhps.server.data.plugin.DefaultSerializers
import net.rwhps.server.io.GameInputStream
import net.rwhps.server.io.GameOutputStream
import java.io.IOException

/**
 * 可调整大小，有序的对象数组
 *
 * 别骂了别骂了 找不到什么好方法
 * 获取同步后得到的就是 ObjectList 只能这样分着写
 * 我不想每次判断
 *
 * @param T
 * @property list ObjectList<T>
 * @property size Int
 */
class Seq<T>: MutableList<T> {
    private val list: BaseSeq<T>

    @JvmOverloads constructor(threadSafety: Boolean = false): this(16, threadSafety)
    @JvmOverloads constructor(capacity: Int, threadSafety: Boolean = false) {
        list = ObjectArrayList<T>(capacity).let { if (threadSafety) ThreadSafety((ObjectLists.synchronize<T>(it,it) as ObjectLists.SynchronizedList<T>),it) else ThreadUnsafe(it) }
    }
    @JvmOverloads constructor(array: Array<T>, threadSafety: Boolean = false) {
        list = ObjectArrayList(array).let { if (threadSafety) ThreadSafety((ObjectLists.synchronize<T>(it,it) as ObjectLists.SynchronizedList<T>),it) else ThreadUnsafe(it) }
    }

    override val size: Int get() = list.size
    override fun add(element: T): Boolean = list.add(element)
    override fun add(index: Int, element: T) = list.add(index, element)
    override fun addAll(elements: Collection<T>): Boolean = list.addAll(elements)
    override fun addAll(index: Int, elements: Collection<T>): Boolean = list.addAll(index, elements)
    fun addElements(index: Int, a: Array<T>) = list.addElements(index, a)
    fun pop(): T = list.pop()
    fun peek(): T = list.peek()
    fun first(): T =list.first()
    override fun isEmpty(): Boolean = list.isEmpty()
    override fun iterator(): MutableIterator<T> = list.iterator()
    override fun listIterator(): MutableListIterator<T> = list.listIterator()
    override fun listIterator(index: Int): MutableListIterator<T> = list.listIterator(index)
    fun any(): Boolean = list.any()
    override fun get(index: Int): T = list[index]
    fun getElements(from: Int, a: Array<T?>, offset: Int, length: Int) = list.getElements(from, a, offset, length)
    override fun indexOf(element: @UnsafeVariance T): Int = list.indexOf(element)
    override fun lastIndexOf(element: @UnsafeVariance T): Int = list.lastIndexOf(element)
    override fun remove(element: T): Boolean = list.remove(element)
    override fun removeAt(index: Int): T = list.removeAt(index)
    override fun removeAll(elements: Collection<T>): Boolean = list.removeAll(elements.toSet())
    fun removeElements(from: Int, to: Int) = list.removeElements(from, to)
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = list.subList(fromIndex, toIndex)
    override fun set(index: Int, element: T): T = list.set(index, element)
    override fun retainAll(elements: Collection<T>): Boolean = list.retainAll(elements.toSet())
    override fun contains(element: T): Boolean = list.contains(element)
    override fun containsAll(elements: Collection<T>): Boolean = list.containsAll(elements)
    fun find(findA: (T)->Boolean): T? = list.find(findA)
    fun find(findA: (T)->Boolean,findB: (T)->Boolean): T? = list.find(findA, findB)
    fun eachAll(block: (T)->Unit) = list.eachAll(block)
    fun eachAllFind(find: (T)->Boolean, block: (T)->Unit) = list.eachAll { if (find(it)) { block(it) } }
    fun eachFind(find: (T)->Boolean, block: (T)->Unit) = list.find(find)?.let { block(it) }
    fun eachAllFinds(findA: (T)->Boolean,findB: (T)->Boolean, block: (T)->Unit) = list.find(findA,findB)?.let { block(it) }
    override fun clear() = list.clear()
    fun <K> toArray(classJava: Class<K>): Array<K> = list.toArray(classJava)

    override fun toString(): String = list.toString()

    companion object {
        private abstract class BaseSeq<T>(private val list: ObjectList<T>){
            val size: Int get() = list.size

            fun add(element: T): Boolean = list.add(element)
            fun add(index: Int, element: T) = list.add(index, element)
            fun addAll(elements: Collection<T>): Boolean = list.addAll(elements)
            fun addAll(index: Int, elements: Collection<T>): Boolean = list.addAll(index, elements)
            /**
             * Add (hopefully quickly) elements to this type-specific list.
             *
             * @param index the index at which to add elements.
             * @param a the array containing the elements.
             */
            fun addElements(index: Int, a: Array<T>) = list.addElements(index, a)

            fun pop(): T = removeAt(size - 1)
            fun peek(): T = get(size - 1)
            fun first(): T = get(0)

            fun iterator(): MutableIterator<T> = list.iterator()
            fun listIterator(): MutableListIterator<T> = list.listIterator()
            fun listIterator(index: Int): MutableListIterator<T> = list.listIterator(index)

            fun any(): Boolean = list.size > 0

            operator fun get(index: Int): T = list[index]

            /**
             * Copies (hopefully quickly) elements of this type-specific list into the given array.
             *
             * @param from the start index (inclusive).
             * @param a the destination array.
             * @param offset the offset into the destination array where to store the first element copied.
             * @param length the number of elements to be copied.
             */
            fun getElements(from: Int, a: Array<T?>, offset: Int, length: Int) = list.getElements(from, a, offset, length)

            fun indexOf(element: @UnsafeVariance T): Int = list.indexOf(element)
            fun lastIndexOf(element: @UnsafeVariance T): Int = list.lastIndexOf(element)

            fun remove(element: T): Boolean = list.remove(element)
            fun removeAt(index: Int): T = list.removeAt(index)
            fun removeAll(elements: Collection<T>): Boolean = list.removeAll(elements.toSet())
            /**
             * Removes (hopefully quickly) elements of this type-specific list.
             *
             * @param from the start index (inclusive).
             * @param to the end index (exclusive).
             */
            fun removeElements(from: Int, to: Int) = list.removeElements(from, to)

            fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = list.subList(fromIndex, toIndex)

            fun set(index: Int, element: T): T = list.set(index, element)

            fun retainAll(elements: Collection<T>): Boolean = list.retainAll(elements.toSet())

            fun contains(element: T): Boolean = list.contains(element)
            fun containsAll(elements: Collection<T>): Boolean = list.containsAll(elements)

            fun isEmpty(): Boolean = size == 0

            abstract fun find(findA: (T)->Boolean): T?
            abstract fun find(findA: (T)->Boolean,findB: (T)->Boolean): T?
            abstract fun eachAll(block: (T)->Unit)

            fun clear() = list.clear()

            abstract fun toArray(): Array<Any>
            abstract fun <K> toArray(classJava: Class<K>): Array<K>

            override fun toString(): String = list.toString()
        }

        private class ThreadUnsafe<T>(private val list: ObjectArrayList<T>) : BaseSeq<T>(list) {
            override fun find(findA: (T)->Boolean): T? {
                list.forEach { if (findA(it)) return it }
                return null
            }
            override fun find(findA: (T)->Boolean,findB: (T)->Boolean): T? {
                list.forEach { if (findA(it) && findB(it)) return it }
                return null
            }
            override fun eachAll(block: (T) -> Unit) {
                list.forEach { block(it) }
            }

            override fun toArray(): Array<Any>  = list.toArray()
            @Suppress("UNCHECKED_CAST")
            override fun <K> toArray(classJava: Class<K>): Array<K> = list.toArray(java.lang.reflect.Array.newInstance(classJava, size) as Array<out K>)

            override fun toString(): String = list.toString()
        }
        private class ThreadSafety<T>(private val list: ObjectLists.SynchronizedList<T>, private val lock: ObjectList<T>) : BaseSeq<T>(list) {
            override fun find(findA: (T)->Boolean): T? {
                synchronized(lock) {
                    list.forEach { if (findA(it)) return it }
                    return null
                }
            }
            override fun find(findA: (T)->Boolean,findB: (T)->Boolean): T? {
                synchronized(lock) {
                    list.forEach { if (findA(it) && findB(it)) return it }
                    return null
                }
            }
            override fun eachAll(block: (T) -> Unit) {
                synchronized(lock) {
                    list.forEach { block(it) }
                }
            }

            override fun toArray(): Array<Any>  = list.toArray()
            @Suppress("UNCHECKED_CAST")
            override fun <K> toArray(classJava: Class<K>): Array<K> = list.toArray(java.lang.reflect.Array.newInstance(classJava, size) as Array<out K>)

            override fun toString(): String = list.toString()
        }

        /*
         * serializer
         */
        val serializer = object : SerializerTypeAll.TypeSerializer<Seq<*>> {
            @Throws(IOException::class)
            override fun write(stream: GameOutputStream, objectData: Seq<*>) {
                stream.writeInt(objectData.size)
                if (objectData.size != 0) {
                    val first = objectData.first()!!
                    val ser = AbstractPluginData.getSerializer(first.javaClass) ?: throw DefaultSerializers.getError(first.javaClass.toString())
                    stream.writeString(first.javaClass.name)
                    for (element in objectData) {
                        ser.write(stream, element)
                    }
                }
            }

            @Throws(IOException::class)
            override fun read(stream: GameInputStream): Seq<*>? {
                return try {
                    val size = stream.readInt()
                    val arr = Seq<Any>(size)
                    if (size == 0) {
                        return arr
                    }
                    val type = stream.readString()
                    val ser = AbstractPluginData.getSerializer(DefaultSerializers.lookup(type)) ?: throw DefaultSerializers.getError(type)

                    for (i in 0 until size) {
                        arr.add(ser.read(stream))
                    }
                    arr
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }
}