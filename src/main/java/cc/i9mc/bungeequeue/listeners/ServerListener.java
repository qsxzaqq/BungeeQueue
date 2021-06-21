package cc.i9mc.bungeequeue.listeners;

import cc.i9mc.bungeequeue.BungeeQueue;
import cc.i9mc.bungeequeue.data.Queue;
import cc.i9mc.bungeequeue.data.QueueJoin;
import cc.i9mc.bungeequeue.data.QueueRequest;
import cc.i9mc.bungeequeue.queue.ServerData;
import cc.i9mc.bungeequeue.queue.ServerHandler;
import cc.i9mc.pluginchannel.events.BungeeCommandEvent;
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class ServerListener implements Listener {

    @EventHandler
    public void onMessage(PubSubMessageEvent event) {
        if (event.getChannel().equals("QueueJoin") && BungeeQueue.getConfig().isMainServer()) {
            BungeeQueue.getInstance().getProxy().getScheduler().runAsync(BungeeQueue.getInstance(), () -> {
                try {
                    QueueJoin queueJoin = BungeeQueue.getGSON().fromJson(event.getMessage(), QueueJoin.class);
                    for (ServerData serverData : ServerHandler.getServersData()) {
                        if (!(serverData.getGroupName().equals(queueJoin.getGroup()) && serverData.getSubName().equals(queueJoin.getSub()))) {
                            continue;
                        }

                        if(serverData.getRedisName() != null){
                            QueueRequest queueRequest = new QueueRequest();
                            queueRequest.setRedisName(serverData.getRedisName());
                            queueRequest.setUuid(queueJoin.getUuid());
                            BungeeQueue.getRedisBungeeAPI().sendChannelMessage("QueueRequest", BungeeQueue.getGSON().toJson(queueRequest));
                            return;
                        }

                        Queue queue = new Queue();
                        queue.setServer(serverData.getServerLink());
                        queue.setUuid(queueJoin.getUuid());
                        BungeeQueue.getRedisBungeeAPI().sendChannelMessage("Queue", BungeeQueue.getGSON().toJson(queue));
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
        } else if (event.getChannel().equals("Queue")) {
            BungeeQueue.getInstance().getProxy().getScheduler().runAsync(BungeeQueue.getInstance(), () -> {
                try {
                    Queue queue = BungeeQueue.getGSON().fromJson(event.getMessage(), Queue.class);
                    ProxiedPlayer player = BungeeQueue.getInstance().getProxy().getPlayer(UUID.fromString(queue.getUuid()));

                    if (player != null) {
                        if (queue.getServer() == null) {
                            player.sendMessage(new TextComponent("§b队列 >> §c匹配失败,当前队列不可用,请重试或选择其他队列!"));
                            return;
                        }

                        player.connect(BungeeQueue.getInstance().getProxy().getServerInfo(queue.getServer()));
                        player.sendMessage(new TextComponent("§b队列 >> §a匹配成功,将您加入 §7" + queue.getServer()));
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
        }
    }

    @EventHandler
    public void onCommand(BungeeCommandEvent e) {
        if (e.getString(0).equalsIgnoreCase("BungeeQueue")) {
            if (e.getString(1).equalsIgnoreCase("join")) {
                ProxiedPlayer player = BungeeQueue.getInstance().getProxy().getPlayer(e.getString(2));
                if (player == null) {
                    return;
                }

                ServerHandler.getServersData().forEach(serverData -> {
                    if (serverData.getGroupName().equals(e.getString(3)) && serverData.getSubName().equals(e.getString(4))) {
                        player.sendMessage(new TextComponent("§b队列 >> §7已将您加入匹配队列"));

                        QueueJoin queueJoin = new QueueJoin();
                        queueJoin.setGroup(e.getString(3));
                        queueJoin.setSub(e.getString(4));
                        queueJoin.setUuid(player.getUniqueId().toString());
                        BungeeQueue.getInstance().getProxy().getScheduler().runAsync(BungeeQueue.getInstance(), () -> BungeeQueue.getRedisBungeeAPI().sendChannelMessage("QueueJoin", BungeeQueue.getGSON().toJson(queueJoin)));
                    }
                });
                return;
            }

            if (e.getString(1).equalsIgnoreCase("data")) {
                BungeeQueue.getInstance().getProxy().getScheduler().runAsync(BungeeQueue.getInstance(), () -> {
                    for (ServerData serverData : ServerHandler.getServersData()) {
                        if(serverData.getGroupName().equals(e.getString(2)) && serverData.getSubName().equals(e.getString(3))){
                            e.response(String.valueOf(serverData.getWaitingPlayers()));
                        }
                    }
                });
            }
        }
    }
}
