package cc.i9mc.bungeequeue.config;

import lombok.Data;

import java.util.LinkedHashMap;

@Data
public class GroupInfo {
    private String name;
    private String displayName;
    private String displayCommand;
    private LinkedHashMap<String, SubInfo> subInfos = new LinkedHashMap<>();

    public void addSubInfo(String name, SubInfo subInfos) {
        this.subInfos.put(name, subInfos);
    }

}
