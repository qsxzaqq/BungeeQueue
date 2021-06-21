package cc.i9mc.bungeequeue.commands;

import cc.i9mc.bungeequeue.BungeeQueue;
import cc.i9mc.bungeequeue.config.Config;
import cc.i9mc.bungeequeue.queue.ServerHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ReloadCommand extends Command {
    public ReloadCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            return;
        }

        BungeeQueue.setConfig(new Config());
        BungeeQueue.getInstance().instantiationConfig();
        ServerHandler.init();
    }
}
