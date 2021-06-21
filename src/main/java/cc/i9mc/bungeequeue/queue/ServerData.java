package cc.i9mc.bungeequeue.queue;

import cc.i9mc.bungeequeue.BungeeQueue;
import cc.i9mc.bungeequeue.serverping.ServerPing;
import cc.i9mc.bungeequeue.serverping.ServerPingEmpty;
import lombok.Getter;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServerData {

    private static Random random = new Random();
    private final String groupName;
    private final String SubName;
    @Getter
    private final String redisName;
    private final Map<String, ServerPing> servers = new ConcurrentHashMap<>();
    private String serverLink;

    public ServerData(String groupName, String SubName, String redisName) {
        this.groupName = groupName;
        this.SubName = SubName;
        this.redisName = redisName;
    }

    public int getOnlineServers() {
        return (int) servers.keySet().stream().filter(this::isServerEffective).count();
    }

    public int getWaitingPlayers() {
        if(getServerLink() == null){
            return -1;
        }

        ServerPing serverPing = servers.getOrDefault(getServerLink(), new ServerPingEmpty());
        if(serverPing instanceof ServerPingEmpty){
            return -1;
        }

        return serverPing.getPlayers();
    }

    public String getEffectiveServer() {
        List<Map.Entry<String, ServerPing>> collect = servers.entrySet().stream().filter(x -> isServerEffective(x.getKey())).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return null;
        }
        collect.sort((b, a) -> Integer.compare(a.getValue().getPlayers(), b.getValue().getPlayers()));
        return collect.get(0).getValue().getPlayers() > 0 ? collect.get(0).getKey() : collect.get(random.nextInt(collect.size())).getKey();
    }

    public boolean isServerOffline(String server) {
        return server == null || servers.getOrDefault(server, new ServerPingEmpty()) instanceof ServerPingEmpty;
    }

    public boolean isServerFull(String server) {
        ServerPing serverPing = servers.getOrDefault(server, new ServerPingEmpty());
        return serverPing.getPlayers() >= serverPing.getMaxplayers();
    }

    public boolean isServerEffective(String server) {
        return !isServerOffline(server) && !isServerFull(server) && getServerState(server) == ServerState.WAITING;
    }

    public ServerState getServerState(String server) {
        if (isServerOffline(server)) {
            return ServerState.OFFLINE;
        } else if (servers.get(server).getDescription().contains(BungeeQueue.getConfig().getMotd())) {
            return ServerState.WAITING;
        } else {
            return ServerState.RUNNING;
        }
    }

    public void updateServers() {
        BungeeQueue.getInstance().getProxy().getScheduler().runAsync(BungeeQueue.getInstance(), () -> {
            if(servers.isEmpty()){
                return;
            }

            for (String server : servers.keySet()) {
                ServerInfo serverInfo = BungeeQueue.getInstance().getProxy().getServerInfo(server);

                if (serverInfo == null) {
                    servers.put(server, new ServerPingEmpty());
                } else {
                    ServerPing serverPing = new ServerPing();
                    serverPing.ping(serverInfo.getAddress(), BungeeQueue.getConfig().getTimeOut());

                    servers.put(server, serverPing.isEnd() ? new ServerPingEmpty() : serverPing);
                }
            }
            if (getServerState(serverLink) != ServerState.WAITING) {
                serverLink = getEffectiveServer();
            }
        });
    }

    public String getGroupName() {
        return groupName;
    }

    public String getSubName() {
        return SubName;
    }

    public Map<String, ServerPing> getServers() {
        return servers;
    }

    public String getServerLink() {
        return serverLink;
    }
}
