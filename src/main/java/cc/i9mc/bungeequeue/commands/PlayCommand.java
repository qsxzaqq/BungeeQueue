package cc.i9mc.bungeequeue.commands;

import cc.i9mc.bungeequeue.BungeeQueue;
import cc.i9mc.bungeequeue.config.GroupInfo;
import cc.i9mc.bungeequeue.config.SubInfo;
import cc.i9mc.bungeequeue.data.QueueJoin;
import cc.i9mc.bungeequeue.queue.ServerData;
import cc.i9mc.bungeequeue.queue.ServerHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayCommand extends Command {

    public PlayCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        if (player.getServer().getInfo().getName().startsWith("Auth-")) {
            return;
        }

        if (args.length == 1) {
            for (GroupInfo groupInfo : BungeeQueue.getConfig().getGroupInfos().values()) {
                if (args[0].equalsIgnoreCase(groupInfo.getDisplayCommand())) {
                    groupInfo.getSubInfos().values().forEach(subInfo -> player.sendMessage(new TextComponent("§e/play " + groupInfo.getDisplayCommand() + " " + subInfo.getDisplayCommand() + " - " + groupInfo.getDisplayName() + " " + subInfo.getDisplayName())));
                    return;
                }
            }
        }

        if (args.length == 2) {
            for (GroupInfo groupInfo : BungeeQueue.getConfig().getGroupInfos().values()) {
                if (!args[0].equalsIgnoreCase(groupInfo.getDisplayCommand())) {
                    continue;
                }

                for (SubInfo subInfo : groupInfo.getSubInfos().values()) {
                    if (!args[1].equalsIgnoreCase(subInfo.getDisplayCommand())) {
                        continue;
                    }

                    for(ServerData serverData : ServerHandler.getServersData()){
                        if (!serverData.getGroupName().equals(groupInfo.getName()) && !serverData.getSubName().equals(subInfo.getName())) {
                            continue;
                        }

                        player.sendMessage(new TextComponent("§b队列 >> §7已将您加入匹配队列"));
                        QueueJoin queueJoin = new QueueJoin();
                        queueJoin.setGroup(groupInfo.getName());
                        queueJoin.setSub(subInfo.getName());
                        queueJoin.setUuid(player.getUniqueId().toString());

                        BungeeQueue.getInstance().getProxy().getScheduler().runAsync(BungeeQueue.getInstance(), () -> BungeeQueue.getRedisBungeeAPI().sendChannelMessage("QueueJoin", BungeeQueue.getGSON().toJson(queueJoin)));
                        return;
                    }
                }
            }
        }


        BungeeQueue.getConfig().getGroupInfos().values().forEach(groupInfo -> groupInfo.getSubInfos().values().forEach(subInfo -> player.sendMessage(new TextComponent("§e/play " + groupInfo.getDisplayCommand() + " " + subInfo.getDisplayCommand() + " - " + groupInfo.getDisplayName() + " " + subInfo.getDisplayName()))));
    }
}
