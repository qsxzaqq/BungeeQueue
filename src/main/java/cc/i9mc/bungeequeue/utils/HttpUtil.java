package cc.i9mc.bungeequeue.utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by JinVan on 2021-01-06.
 */
public class HttpUtil {
    public static InputStream downloadFromUrl(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
        conn.setConnectTimeout(3000);
        return conn.getInputStream();
    }
}
