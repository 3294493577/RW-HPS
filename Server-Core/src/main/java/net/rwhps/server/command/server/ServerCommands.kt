/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *  
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.command.server

import net.rwhps.server.core.Call
import net.rwhps.server.core.Call.sendSystemMessage
import net.rwhps.server.core.Call.sendTeamData
import net.rwhps.server.core.Call.upDataGameData
import net.rwhps.server.data.ModManage
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.Data.LINE_SEPARATOR
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.data.player.Player
import net.rwhps.server.data.plugin.PluginManage
import net.rwhps.server.func.StrCons
import net.rwhps.server.game.event.EventType.GameOverEvent
import net.rwhps.server.game.event.EventType.PlayerBanEvent
import net.rwhps.server.net.core.server.AbstractNetConnect
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.Font16
import net.rwhps.server.util.IsUtils
import net.rwhps.server.util.Time.getTimeFutureMillis
import net.rwhps.server.util.game.CommandHandler
import net.rwhps.server.util.game.Events
import net.rwhps.server.util.log.Log.error
import java.io.IOException

/**
 * @author RW-HPS/Dr
 */
internal class ServerCommands(handler: CommandHandler) {
    private fun registerPlayerCommand(handler: CommandHandler) {
        handler.register("say", "<text...>", "serverCommands.say") { arg: Array<String>, _: StrCons ->
            val response = StringBuilder(arg[0])
            var i = 1
            val lens = arg.size
            while (i < lens) {
                response.append(" ").append(arg[i])
                i++
            }
            if (Data.config.SingleUserRelay) {
                try {
                    NetStaticData.relay.groupNet.broadcast(NetStaticData.RwHps.abstractNetPacket.getSystemMessagePacket(response.toString().replace("<>", "")))
                } catch (ignored: IOException) {
                }
            }
            sendSystemMessage(response.toString().replace("<>", ""))
        }
        handler.register("gameover", "serverCommands.gameover") { _: Array<String>?, _: StrCons ->
            Events.fire(GameOverEvent(null))
        }
        handler.register("admin", "<add/remove> <PlayerPosition> [SpecialPermissions]", "serverCommands.admin") { arg: Array<String>, log: StrCons ->
            if (Data.game.isStartGame) {
                log[localeUtil.getinput("err.startGame")]
                return@register
            }
            if (!("add" == arg[0] || "remove" == arg[0])) {
                log["Second parameter must be either 'add' or 'remove'."]
                return@register
            }
            val add = "add" == arg[0]
            val site = arg[1].toInt() - 1
            val player = Data.game.playerManage.getPlayerArray(site)
            val supAdmin = arg.size > 2
            if (player != null) {
                if (add) {
                    Data.core.admin.addAdmin(player.uuid,supAdmin)
                } else {
                    Data.core.admin.removeAdmin(player.uuid)
                }

                player.autoAdmin = false
                player.isAdmin = add
                player.superAdmin = supAdmin

                try {
                    player.con!!.sendServerInfo(false)
                } catch (e: IOException) {
                    error("[Player] Send Server Info Error", e)
                }
                sendTeamData()
                log["Changed admin status of player: {0}", player.name]
            }
        }
        handler.register("clearbanall", "serverCommands.clearbanall") { _: Array<String>?, _: StrCons ->
            Data.core.admin.bannedIPs.clear()
            Data.core.admin.bannedUUIDs.clear()
        }
        handler.register("ban", "<PlayerPositionNumber>", "serverCommands.ban") { arg: Array<String>, _: StrCons ->
            val site = arg[0].toInt() - 1
            val player = Data.game.playerManage.getPlayerArray(site)
            if (player != null) {
                Events.fire(PlayerBanEvent(player))
            }
        }
        handler.register("mute", "<PlayerPositionNumber> [Time(s)]", "serverCommands.mute") { arg: Array<String>, _: StrCons ->
            val site = arg[0].toInt() - 1
            val player = Data.game.playerManage.getPlayerArray(site)
            if (player != null) {
                //Data.game.playerData[site].muteTime = getLocalTimeFromU(Long.parseLong(arg[1])*1000L);
                player.muteTime = getTimeFutureMillis(43200 * 1000L)
            }
        }
        handler.register("kick", "<PlayerPositionNumber> [time]", "serverCommands.kick") { arg: Array<String>, _: StrCons ->
            val site = arg[0].toInt() - 1
            val player = Data.game.playerManage.getPlayerArray(site)
            if (player != null) {
                player.kickTime = if (arg.size > 1) getTimeFutureMillis(
                    arg[1].toInt() * 1000L
                ) else getTimeFutureMillis(60 * 1000L)
                try {
                    player.kickPlayer(localeUtil.getinput("kick.you"))
                } catch (e: IOException) {
                    error("[Player] Send Kick Player Error", e)
                }
            }
        }
        handler.register("isafk", "<off/on>", "serverCommands.isAfk") { arg: Array<String>, _: StrCons ->
            if (Data.configServer.OneAdmin) {
                Data.game.isAfk = "on" == arg[0]
            }
        }
        handler.register("maplock", "<off/on>", "serverCommands.maplock") { arg: Array<String>, _: StrCons ->
            Data.game.mapLock = "on" == arg[0]
        }
        handler.register("battleroyal", "<off/on>", "serverCommands.battleRoyalLock") { arg: Array<String>, _: StrCons ->
            Data.game.battleRoyalLock = "on" == arg[0]
        }
        handler.register("kill", "<PlayerPositionNumber>", "serverCommands.kill") { arg: Array<String>, log: StrCons ->
            if (Data.game.isStartGame) {
                val site = arg[0].toInt() - 1
                val player = Data.game.playerManage.getPlayerArray(site)
                if (player != null) {
                    player.con!!.sendSurrender()
                }
            } else {
                log[localeUtil.getinput("err.noStartGame")]
            }
        }
        handler.register("giveadmin", "<PlayerPositionNumber...>", "serverCommands.giveadmin") { arg: Array<String>, _: StrCons ->
            Data.game.playerManage.playerGroup.eachAllFind(
                { p: Player -> p.isAdmin }) { i: Player ->
                val player = Data.game.playerManage.getPlayerArray(arg[0].toInt())
                if (player != null) {
                    i.isAdmin = false
                    player.isAdmin = true
                    upDataGameData()
                    Call.sendSystemMessageLocal("give.ok", player.name)
                }
            }
        }
        handler.register("clearmuteall", "serverCommands.clearmuteall") { _: Array<String>?, _: StrCons ->
            Data.game.playerManage.playerGroup.eachAll { e: Player -> e.muteTime = 0 }
        }

        handler.register("turnstoneintogold", "<PlayerPositionNumber>", "# turnstoneintogold") { arg: Array<String>, _: StrCons ->
            val site = arg[0].toInt() - 1
            val player = Data.game.playerManage.getPlayerArray(site)
            if (player != null) {
                player.turnStoneIntoGold = true
            }
        }
    }

    private fun registerPlayerStatusCommand(handler: CommandHandler) {
        handler.register("players", "serverCommands.players") { _: Array<String>?, log: StrCons ->
            if (Data.game.playerManage.playerGroup.size == 0) {
                log["No players are currently in the server."]
            } else {
                log["Players: {0}", Data.game.playerManage.playerGroup.size]
                val data = StringBuilder()
                for (player in Data.game.playerManage.playerGroup) {
                    data.append(LINE_SEPARATOR)
                        .append(player.name)
                        .append(" / ")
                        .append("Position: ").append(player.site)
                        .append(" / ")
                        .append("IP: ").append((player.con!! as AbstractNetConnect).ip)
                        .append(" / ")
                        .append("Protocol: ").append((player.con!! as AbstractNetConnect).useConnectionAgreement)
                        .append(" / ")
                        .append("Admin: ").append(player.isAdmin)
                }
                log[data.toString()]
            }
        }

        handler.register("admins", "serverCommands.admins") { _: Array<String>?, log: StrCons ->
            if (Data.core.admin.playerAdminData.size == 0) {
                log["No admins are currently in the server."]
            } else {
                log["Admins: {0}", Data.core.admin.playerAdminData.size]
                val data = StringBuilder()
                for (player in Data.core.admin.playerAdminData.values) {
                    data.append(LINE_SEPARATOR)
                        .append(player.name)
                        .append(" / ")
                        .append("ID: ").append(player.uuid)
                        .append(" / ")
                        .append("Admin: ").append(player.admin)
                        .append(" / ")
                        .append("SuperAdmin: ").append(player.superAdmin)
                }
                log[data.toString()]
            }
        }

        handler.register("reloadmods", "serverCommands.reloadmods") { _: Array<String>?, log: StrCons ->
            if (Data.game.isStartGame) {
                log[Data.i18NBundle.getinput("err.startGame")]
            } else {
                log[Data.i18NBundle.getinput("server.loadMod", ModManage.reLoadMods())]
                upDataGameData(true)
            }

        }
        handler.register("reloadmaps", "serverCommands.reloadmaps") { _: Array<String>?, log: StrCons ->
            val size = Data.game.mapsData.size
            Data.game.mapsData.clear()
            Data.game.checkMaps()
            // Reload 旧列表的Custom数量 : 新列表的Custom数量
            log["Reload Old Size:New Size is {0}:{1}", size, Data.game.mapsData.size]
        }
    }

    private fun registerPlayerCustomEx(handler: CommandHandler) {
        handler.register("summon", "<unitName> <x> <y> [index(NeutralByDefault)]", "serverCommands.summon") { arg: Array<String>, log: StrCons ->
            if (!Data.game.isStartGame) {
                log[localeUtil.getinput("err.noStartGame")]
                return@register
            }
            val index = if (arg.size > 3) {
                when {
                    IsUtils.isNumericNegative(arg[3]) -> arg[3].toInt()
                    else -> -1
                }
            } else {
                -1
            }
            if (IsUtils.notIsNumeric(arg[1]) || IsUtils.notIsNumeric(arg[2])) {
                log["Not Numeric, Invalid coordinates"]
                return@register
            }
            Data.game.gameCommandCache.add(NetStaticData.RwHps.abstractNetPacket.gameSummonPacket(index,arg[0],arg[1].toFloat(),arg[2].toFloat()))
        }

        handler.register("textbuild", "<UnitName> <Text> [index(NeutralByDefault)]", "serverCommands.textbuild") { arg: Array<String>, _: StrCons ->
            val cache = Seq<Array<ByteArray>>()

            arg[1].forEach {
                cache.add(Font16.resolveString(it.toString()))
            }

            val index = if (arg.size > 2) {
                when {
                    IsUtils.isNumericNegative(arg[2]) -> arg[2].toInt()
                    else -> -1
                }
            } else {
                -1
            }

            // 偏移量
            var off = 0

            cache.eachAll {
                var i = 0
                var lg = true
                for ((height, lineArray) in it.withIndex()) {
                    for ((width, b) in lineArray.withIndex()) {
                        if (lg) {
                            i++
                        }
                        if (b.toInt() == 1) {
                            Data.game.gameCommandCache.add(NetStaticData.RwHps.abstractNetPacket.gameSummonPacket(index, arg[0], ((off+width)*20).toFloat(), (height*20).toFloat()))
                        }
                    }
                    lg = false
                }
                i++
                off += i
            }
        }

        handler.register("addmoney", "<PlayerPositionNumber> <money>", "serverCommands.addmoney") { arg: Array<String>, log: StrCons ->
            if (!Data.game.isStartGame) {
                log[localeUtil.getinput("err.noStartGame")]
                return@register
            }
            val site = arg[0].toInt() - 1
            val player = Data.game.playerManage.getPlayerArray(site)
            if (player != null) {
                player.credits += arg[1].toInt()
                player.sync()
            }
        }
    }

    companion object {
        private val localeUtil = Data.i18NBundle
    }

    init {
        registerPlayerCommand(handler)
        registerPlayerStatusCommand(handler)
        registerPlayerCustomEx(handler)

        PluginManage.runRegisterServerCommands(handler)

        ClientCommands(Data.CLIENT_COMMAND)
    }
}