package cc.i9mc.bungeequeue;

import cc.i9mc.bungeequeue.commands.PlayCommand;
import cc.i9mc.bungeequeue.commands.ReloadCommand;
import cc.i9mc.bungeequeue.config.Config;
import cc.i9mc.bungeequeue.config.GroupInfo;
import cc.i9mc.bungeequeue.config.SubInfo;
import cc.i9mc.bungeequeue.listeners.ServerListener;
import cc.i9mc.bungeequeue.queue.ServerHandler;
import cc.i9mc.bungeequeue.utils.HttpUtil;
import cc.i9mc.bungeequeue.yaml.Configuration;
import cc.i9mc.bungeequeue.yaml.YamlConfiguration;
import com.google.gson.Gson;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;

public final class BungeeQueue extends Plugin {
    @Getter
    private static BungeeQueue instance;
    @Getter
    private static RedisBungeeAPI redisBungeeAPI;
    @Getter
    private static Gson GSON = new Gson();
    @Getter
    @Setter
    private static Config config;

    @Override
    public void onEnable() {
        instance = this;

        redisBungeeAPI = RedisBungee.getApi();
        redisBungeeAPI.registerPubSubChannels("Queue");
        redisBungeeAPI.registerPubSubChannels("QueueJoin");

        config = new Config();

        instantiationConfig();

        ServerHandler.init();

        getProxy().getPluginManager().registerCommand(this, new PlayCommand("play"));
        getProxy().getPluginManager().registerCommand(this, new ReloadCommand("qreload"));
        getProxy().getPluginManager().registerListener(this, new ServerListener());
    }

    @SneakyThrows
    public void instantiationConfig() {
        Configuration configuration = YamlConfiguration.loadConfiguration(HttpUtil.downloadFromUrl("http://yxsj-file:9000/configs/GameQueue.yml"));

        config.setMainServer(redisBungeeAPI.getServerId().equals(configuration.getString("mainServer")));
        config.setMotd(configuration.getString("motd"));
        config.setPingDelay(configuration.getInt("pingDelay"));
        config.setTimeOut(configuration.getInt("timeOut"));
        config.getGroupInfos().clear();

        configuration.getConfigurationSection("group").getKeys(false).forEach(groupName -> {
            GroupInfo groupInfo = new GroupInfo();

            groupInfo.setName(groupName);
            groupInfo.setDisplayName(configuration.getString("group." + groupName + ".name"));
            groupInfo.setDisplayCommand(configuration.getString("group." + groupName + ".command"));

            configuration.getConfigurationSection("group." + groupName + ".sub").getKeys(false).forEach(subName -> {
                SubInfo subInfo = new SubInfo();

                subInfo.setName(subName);
                subInfo.setDisplayName(configuration.getString("group." + groupName + ".sub." + subName + ".name"));
                subInfo.setDisplayCommand(configuration.getString("group." + groupName + ".sub." + subName + ".command"));

                if(configuration.contains("group." + groupName + ".sub." + subName + ".servers")){
                    configuration.getStringList("group." + groupName + ".sub." + subName + ".servers").forEach(subInfo::addServer);
                }else {
                    subInfo.setRedisSend(configuration.getString("group." + groupName + ".sub." + subName + ".redis"));
                }

                groupInfo.addSubInfo(subName, subInfo);
            });

            config.addGroupInfo(groupName, groupInfo);
        });
    }
}
