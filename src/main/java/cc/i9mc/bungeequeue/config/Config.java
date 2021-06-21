package cc.i9mc.bungeequeue.config;

import lombok.Data;

import java.util.LinkedHashMap;

@Data
public class Config {
    private boolean mainServer;
    private String motd;
    private int pingDelay;
    private int timeOut;
    private LinkedHashMap<String, GroupInfo> groupInfos = new LinkedHashMap<>();

    public void addGroupInfo(String name, GroupInfo groupInfos) {
        this.groupInfos.put(name, groupInfos);
    }
}
