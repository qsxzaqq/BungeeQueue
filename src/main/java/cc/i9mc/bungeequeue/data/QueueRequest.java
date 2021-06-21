package cc.i9mc.bungeequeue.data;

import lombok.Data;

/**
 * Created by JinVan on 2021-01-06.
 */
@Data
public class QueueRequest {
    private String redisName;
    private String uuid;
}
