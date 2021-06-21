package cc.i9mc.bungeequeue.bukkit;

import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class PlaceholderHook extends EZPlaceholderHook {
    public PlaceholderHook(Plugin plugin, String identifier) {
        super(plugin, identifier);
    }

    @Override
    public String onPlaceholderRequest(Player player, String s) {
        String[] strings = s.split("_");

        if (ServerHandler.getServersData().isEmpty()) {
            ServerHandler.addServerData(strings[0], strings[1]);
            return "-1";
        }

        for (Map.Entry<String[], Integer> entry : ServerHandler.getServersData().entrySet()) {
            if (entry.getKey()[0].equalsIgnoreCase(strings[0]) && entry.getKey()[1].equalsIgnoreCase(strings[1])) {
                return String.valueOf(entry.getValue() == 0 ? -1 : entry.getValue());
            }
        }

        ServerHandler.addServerData(strings[0], strings[1]);
        return null;
    }
}
