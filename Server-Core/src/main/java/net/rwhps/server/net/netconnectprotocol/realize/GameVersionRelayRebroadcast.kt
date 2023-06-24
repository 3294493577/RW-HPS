/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.net.netconnectprotocol.realize

import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.io.GameInputStream
import net.rwhps.server.io.GameOutputStream
import net.rwhps.server.io.packet.Packet
import net.rwhps.server.net.core.ConnectionAgreement
import net.rwhps.server.struct.ObjectMap
import net.rwhps.server.util.PacketType
import net.rwhps.server.util.log.Log
import net.rwhps.server.util.log.Log.error
import java.io.IOException
import java.util.*

/**
 * Many thanks to them for providing cloud computing for the project
 * This is essential to complete the RW-HPS Relay test
 * @Thanks : [SimpFun Cloud](https://vps.tiexiu.xyz)
 * @Thanks : [Github 1dNDN](https://github.com/1dNDN)
 *
 * This test was done on :
 * Relay-CN (V. 6.1.0)
 * 2022.7.22 10:00
 */

/**
 * Relay protocol implementation
 * Can realize multicast and save bandwidth
 *
 * @property lastSentPacket Last packet sent
 * @property version        Protocol version
 * @constructor
 *
 * @author RW-HPS/Dr
 */
class GameVersionRelayRebroadcast(connectionAgreement: ConnectionAgreement) : GameVersionRelay(connectionAgreement) {
    // last Unwrapped Or Normal Packet From Server
    //@get:Synchronized
    private lateinit var lastSentPacket: Packet

    override val version: String
        get() = "1.14 RELAY Rebroadcast"

    override fun setlastSentPacket(packet: Packet) {
        lastSentPacket = packet
    }

    override fun sendRelayServerId() {
        try {
            inputPassword = false
            if (relay == null) {
                Log.clog("sendRelayServerId -> relay : null")
                relay = NetStaticData.relay
            }

            if (site != -1) {
                Log.debug("Remove Move Player $site, HOST Yes")
                relay!!.removeAbstractNetConnect(site)
                site = -1
            }

            if (relay!!.admin != null) {
                relayKickData = relay!!.admin!!.relayKickData
                relayPlayersData = relay!!.admin!!.relayPlayersData
            } else {
                relayKickData = ObjectMap()
                relayPlayersData = ObjectMap()
            }

            relay!!.admin = this

            val o = GameOutputStream()
            if (clientVersion >= version2) {
                o.writeByte(2)
                o.writeBoolean(true)
                o.writeBoolean(true)
                o.writeBoolean(true)
                o.writeString(relay!!.serverUuid)
                o.writeBoolean(relay!!.isMod) //MOD
                o.writeBoolean(false)
                o.writeBoolean(true)
                o.writeString("{{RW-HPS Relay}}.Room ID : ${Data.configRelay.MainID}" + relay!!.id)
                o.writeBoolean(true)
                o.writeIsString(registerPlayerId)
            } else {
                o.writeByte(1)
                o.writeBoolean(true)
                o.writeBoolean(true)
                o.writeBoolean(true)
                o.writeString(relay!!.serverUuid)
                o.writeBoolean(relay!!.isMod) //MOD
                // List OPEN
                o.writeBoolean(false)
                o.writeBoolean(true)
                o.writeString("{{RW-HPS Relay}}.Room ID : ${Data.configRelay.MainID}" + relay!!.id)
                // 多播
                o.writeBoolean(true)
            }
            sendPacket(o.createPacket(PacketType.RELAY_BECOME_SERVER)) //+108+140
            sendPacket(NetStaticData.RwHps.abstractNetPacket.getChatMessagePacket(Data.i18NBundle.getinput("relay.server.admin.connect", Data.configRelay.MainID+relay!!.id, Data.configRelay.MainID+relay!!.internalID.toString()), "RELAY_CN-ADMIN", 5))
            sendPacket(NetStaticData.RwHps.abstractNetPacket.getChatMessagePacket(Data.i18NBundle.getinput("relay", Data.configRelay.MainID+relay!!.id), "RELAY_CN-ADMIN", 5))


            // 人即像树，树枝越向往光明的天空，树根越伸向阴暗的地底
            /**
             * 禁止玩家使用 Server/Relay 做玩家名
             */
            if (name.equals("SERVER", ignoreCase = true) || name.equals("RELAY", ignoreCase = true)) {
                relay!!.groupNet.disconnect() // Close Room
                disconnect() // Close Connect & Reset Room
            }

        } catch (e: Exception) {
            error(e)
        }
    }

    override fun addRelaySend(packet: Packet) {
        try {
            GameInputStream(packet).use { inStream ->
                val target = inStream.readInt()
                val type = inStream.readInt()

                if (type == PacketType.DISCONNECT.typeInt) {
                    return
                }

                val bytes = inStream.readStreamBytes()
                val abstractNetConnect = relay!!.getAbstractNetConnect(target)


                Packet(type, bytes).let { sendPacketData ->
                    abstractNetConnect?.sendPacket(sendPacketData)
                    lastSentPacket = sendPacketData
                    sendPacketExtractInformation(sendPacketData,abstractNetConnect)
                }
            }
        } catch (_: IOException) {
            /* 忽略 */
        } catch (_: NullPointerException) {
            /* 忽略 */
        }
    }

    @Throws(IOException::class)
    override fun multicastAnalysis(packet: Packet) {
        try {
            GameInputStream(packet).use { stream ->
                val target = stream.readInt()
                relay!!.getAbstractNetConnect(target)?.sendPacket(lastSentPacket)
            }
        } catch (_: IOException) {
            /* 忽略 */
        }
    }
}
