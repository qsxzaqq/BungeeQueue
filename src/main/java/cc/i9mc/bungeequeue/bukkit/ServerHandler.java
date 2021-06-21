package cc.i9mc.bungeequeue.bukkit;

import cc.i9mc.pluginchannel.BukkitChannel;
import cc.i9mc.pluginchannel.bukkit.PBukkitChannelTask;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler {

    private static final Map<String[], Integer> servers = new ConcurrentHashMap<>();

    public static void init() {
        new BukkitRunnable() {

            @Override
            public void run() {
                if (Bukkit.getOnlinePlayers().isEmpty()) {
                    return;
                }

                for(Map.Entry<String[], Integer> e : servers.entrySet()){
                    PBukkitChannelTask.createTask()
                            .channel(BukkitChannel.getInst().getBukkitChannel())
                            .sender(Bukkit.getOnlinePlayers().iterator().next())
                            .command("BungeeQueue", "data", e.getKey()[0], e.getKey()[1])
                            .result((result) -> servers.put(e.getKey(), Integer.valueOf(result[0]))).run();
                }
            }
        }.runTaskTimerAsynchronously(BukkitChannel.getInst(), 0, 20);
    }

    public static void addServerData(String GroupName, String SubName) {
        servers.put(new String[]{GroupName, SubName}, -1);
    }

    public static Map<String[], Integer> getServersData() {
        return servers;
    }
}
