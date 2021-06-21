package cc.i9mc.bungeequeue;

import cc.i9mc.bungeequeue.bukkit.PlaceholderHook;
import cc.i9mc.bungeequeue.bukkit.ServerHandler;
import cc.i9mc.bungeequeue.commands.QueueCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitQueue extends JavaPlugin {

    @Override
    public void onEnable() {
        ServerHandler.init();
        Bukkit.getPluginCommand("queue").setExecutor(new QueueCommand());

        if(getServer().getPluginManager().getPlugin("PlaceholderAPI") != null){
            new PlaceholderHook(this, "Queue").hook();
        }
    }
}
