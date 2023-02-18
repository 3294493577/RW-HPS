/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.net.code.tcp

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.util.ReferenceCountUtil
import net.rwhps.server.io.packet.Packet
import net.rwhps.server.net.NetService
import net.rwhps.server.util.PacketType
import net.rwhps.server.util.log.Log.warn

/**
 * Parse game packets
 * @author RW-HPS/Dr
 */
/**
 *    1 2 3 4  5  6  7  8  ...
 *   +-+-+-+-+-+-+-+-+---------------+
 *   |0 |0 |0 |0 |0|0|0|0| Data|
 *   +-+-+-+-+-+-+-+-+---------------+
 *   |Data length|  Type | Data
 *   +---------------+---------------+
 */
internal class PacketDecoder : ByteToMessageDecoder() {
    companion object {
        /** Packet header data length */
        private const val HEADER_SIZE = 8
    }

    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, bufferIn: ByteBuf?, out: MutableList<Any>) {
        if (bufferIn == null) {
            return
        }

        val readableBytes = bufferIn.readableBytes()
        if (readableBytes < HEADER_SIZE) {
            return
        }

        /*
         * Someone may be sending a lot of packets to the server to take up broadband
         * Close the connection directly by default
         *
         * Maximum accepted single package size = 50 MB
         */
        if (readableBytes > NetService.maxPacketSizt) {
            warn("Package size exceeds maximum")
            ReferenceCountUtil.release(bufferIn)
            ctx.close()
            return
        }
        val readerIndex = bufferIn.readerIndex()
        val contentLength = bufferIn.readInt()
        val type = bufferIn.readInt()

        /*
         * This packet is an error packet and should not be present so disconnect
         */
        if (contentLength < 0 || type < 0) {
            ctx.close()
            return
        }


        /*
         * Insufficient data length, reset the identification bit and read again
         */
        if (readableBytes < contentLength) {
            bufferIn.readerIndex(readerIndex)
            return
        }

        val packetType = PacketType.from(type)
        if (packetType == PacketType.NOT_RESOLVED) {
            ctx.close()
            return
        }

        val b = ByteArray(contentLength)
        bufferIn.readBytes(b)
        // 读完就甩
        //bufferIn.discardReadBytes()
        out.add(Packet(packetType, b))
    }
}