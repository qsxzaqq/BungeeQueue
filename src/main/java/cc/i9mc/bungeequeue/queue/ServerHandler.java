package cc.i9mc.bungeequeue.queue;

import cc.i9mc.bungeequeue.BungeeQueue;
import cc.i9mc.bungeequeue.serverping.ServerPingEmpty;
import cc.i9mc.pluginchannel.logger.PLogger;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServerHandler {

    private static ScheduledTask scheduledTask;
    private static final List<ServerData> serversData = new ArrayList<>();

    public static void init() {
        serversData.clear();
        reloadServer();

        if(scheduledTask != null) scheduledTask.cancel();
        scheduledTask = BungeeQueue.getInstance().getProxy().getScheduler().schedule(BungeeQueue.getInstance(), () -> serversData.forEach(ServerData::updateServers), 0, BungeeQueue.getConfig().getPingDelay(), TimeUnit.MILLISECONDS);
    }

    public static void reloadServer() {
        BungeeQueue.getConfig().getGroupInfos().values().forEach(groupInfo -> groupInfo.getSubInfos().values().forEach(subInfo -> {
            ServerData serverData = new ServerData(groupInfo.getName(), subInfo.getName(), subInfo.getRedisSend());
            if(subInfo.getRedisSend() == null){
                subInfo.getServers().forEach(server -> serverData.getServers().put(server, new ServerPingEmpty()));
            }
            serversData.add(serverData);
        }));
        PLogger.info("Loaded " + serversData.size() + " Link Server");
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static List<ServerData> getServersData() {
        return serversData;
    }
}
