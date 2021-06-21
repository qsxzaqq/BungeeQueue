package cc.i9mc.bungeequeue.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SubInfo {
    private String name;
    private String displayName;
    private String displayCommand;
    private String redisSend = null;
    private List<String> servers = new ArrayList<>();

    public void addServer(String server){
        servers.add(server);
    }
}
