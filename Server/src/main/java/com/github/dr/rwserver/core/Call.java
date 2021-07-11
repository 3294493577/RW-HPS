package com.github.dr.rwserver.core;

import com.github.dr.rwserver.core.thread.Threads;
import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.data.global.NetStaticData;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.game.GameCommand;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.game.Events;
import com.github.dr.rwserver.util.log.Log;
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author Dr
 */
public class Call {

    public static void sendMessage(Player player, String text) {
        try {
            NetStaticData.groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.getChatMessagePacket(text,player.name,player.team));
        } catch (IOException e) {
            Log.error("[ALL] Send Player Chat Error",e);
        }
    }

    public static void sendMessageLocal(Player player, String text, Object... obj) {
        Data.playerGroup.each(e -> e.sendMessage(player,e.localeUtil.getinput(text,obj)));
    }

    public static void sendTeamMessage(int team,Player player, String text) {
        Data.playerGroup.eachBooleanIfs(e -> e.team == team, p -> p.sendMessage(player,"[TEAM] "+text));
    }

    public static void sendSystemTeamMessageLocal(int team,String text, Object... obj) {
        Data.playerGroup.eachBooleanIfs(e -> e.team == team, p -> p.sendSystemMessage("[TEAM] "+p.localeUtil.getinput(text,obj)));
    }

    public static void sendSystemMessage(String text) {
        try {
            NetStaticData.groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.getSystemMessagePacket(text));
        } catch (IOException e) {
            Log.error("[ALL] Send System Chat Error",e);
        }
    }

    public static void sendSystemMessageLocal(String text, Object... obj) {
        Data.playerGroup.each(e -> e.sendSystemMessage(e.localeUtil.getinput(text,obj)));
    }

    public static void sendSystemMessage(String text, Object... obj) {
        Data.playerGroup.each(e -> e.sendSystemMessage(e.localeUtil.getinput(text,obj)));
    }

    public static void sendTeamData() {
        if (Data.game.reConnectBreak) {
            return;
        }
        try {
            GzipEncoder enc = NetStaticData.protocolData.abstractNetPacket.getTeamDataPacket();
            Data.playerGroup.each(e -> e.con.sendTeamData(enc));
        } catch (IOException e) {
            Log.error("[ALL] Send Team Error",e);
        }
    }

    public static void sendPlayerPing() {
        Data.playerGroup.each(e -> e.con.ping());
    }

    public static void upDataGameData() {
        Data.playerGroup.each(e -> {
            try {
                e.con.sendServerInfo(false);
            } catch (IOException err) {
                Log.error("[ALL] Send System Info Error",err);
            }
        });

    }

    public static void killAllPlayer() {
        Data.playerGroup.each(e -> {
            try {
                e.con.sendKick("Game Over");
            } catch (IOException err) {
                Log.error("[ALL] Kick All Player Error",e);
            }
        });
    }

    public static void disAllPlayer() {
        Data.playerGroup.each(e -> e.con.disconnect());
    }

    public static void testPreparationPlayer() {
        new Timer().schedule(new RandyTask(), 0, 100);
    }

    private static class RandyTask extends TimerTask {

        private int loadTime = 0;
        private final int loadTimeMaxTry = 30;
        private boolean start =true;
        @Override
        public void run() {
            Data.playerGroup.each(p -> {
                if (!p.start) {
                    loadTime += 1;
                    if (loadTime > loadTimeMaxTry) {
                        if (start) {
                            start = false;
                            Call.sendSystemMessageLocal("start.testNo");
                        }
                        Threads.newThreadService2(new SendGameTickCommand(),0,150, TimeUnit.MILLISECONDS,"GameTask");
                        cancel();
                    }
                }
            });
            if (start) {
                start = false;
                Call.sendSystemMessageLocal("start.testYes");
            }
            Threads.newThreadService2(new SendGameTickCommand(),0,150, TimeUnit.MILLISECONDS,"GameTask");
            cancel();
        }
    }

    private static class SendGameTickCommand implements Runnable {
        private int time = 0;
        private boolean oneSay = true;
        private boolean gameOver = true;
        @Override
        public void run() {
            if (Data.game.reConnectBreak) {
                return;
            }
            time += 10;
            if (Data.playerGroup.size() == 0) {
                Events.fire(new EventType.GameOverEvent());
                return;
            }
            if (Data.playerGroup.size() <= 1) {
                if (oneSay) {
                    oneSay = false;
                    Call.sendSystemMessageLocal("gameOver.oneMin");
                }
                if (gameOver) {
                    gameOver = false;
                    Threads.newThreadService(() -> Events.fire(new EventType.GameOverEvent()),1, TimeUnit.MINUTES,"Gameover");
                }
            } else {
                if (Threads.getIfScheduledFutureData("Gameover")) {
                    oneSay = true;
                    gameOver = true;
                    Threads.removeScheduledFutureData("Gameover");
                }
            }
            final int size = Data.game.gameCommandCache.size();
            if (size == 0) {
                try {
                    NetStaticData.groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.getTickPacket(time));
                } catch (IOException e) {
                    Log.error("[ALL] Send Tick Failed",e);
                }
            } else if (size == 1 ) {
                GameCommand gameCommand = Data.game.gameCommandCache.poll();
                try {
                    NetStaticData.groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.getGameTickCommandPacket(time,gameCommand));
                } catch (IOException e) {
                    Log.error("[ALL] Send Game Tick Error",e);
                }
            } else {
                Seq<GameCommand> comm = new Seq<>(size);
                IntStream.range(0, size).mapToObj(i -> Data.game.gameCommandCache.poll()).forEach(comm::add);
                try {
                    NetStaticData.groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.getGameTickCommandsPacket(time,comm));
                } catch (IOException e) {
                    Log.error("[ALL] Send Game Ticks Error",e);
                }
            }
        }
    }
}
