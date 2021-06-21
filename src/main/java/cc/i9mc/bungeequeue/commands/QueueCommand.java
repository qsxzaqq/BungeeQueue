package cc.i9mc.bungeequeue.commands;

import cc.i9mc.pluginchannel.BukkitChannel;
import cc.i9mc.pluginchannel.BungeeChannel;
import cc.i9mc.pluginchannel.bukkit.PBukkitChannelTask;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class QueueCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args[0].equalsIgnoreCase("join")) {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage("§4Command cannot used on Console.");
                return true;
            }

            if (args.length == 1) {
                sender.sendMessage("§4Invalid arguments.");
                return true;
            }

            PBukkitChannelTask.createTask()
                    .channel(BukkitChannel.getInst().getBukkitChannel())
                    .command("BungeeQueue", "join", sender.getName(), args[1], args[2])
                    .sender((Player) sender)
                    .run();
        }
        return true;
    }
}
