package com.pie.tlatoani.Skin.MineSkin;

import com.pie.tlatoani.Mundo;
import com.pie.tlatoani.Skin.Skin;
import mundosk_libraries.jsoup_light.Connection;
import mundosk_libraries.jsoup_light.HttpConnection;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by Tlatoani on 5/7/17.
 * Based on (and with portions of code copied from) the Mineskin Client created by inventivetalent (who also created the Mineskin API)
 */
public class MineSkinClient {
    public static final String SKIN_OPTIONS = "name=&model=DEFAULT&visibility=PUBLIC";
    public static final String URL_FORMAT = "https://api.mineskin.org/generate/url?url=%s&%s";
    public static final String UPLOAD_FORMAT = "https://api.mineskin.org/generate/upload?%s";
    public static final String USER_AGENT = "MineSkin-JavaClient";

    public static String rawStringFromURL(String url) {
        try {
            Connection connection = HttpConnection
                    .connect(String.format(URL_FORMAT, url, SKIN_OPTIONS))
                    .userAgent(USER_AGENT)
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .timeout(10000);
            return connection.execute().body();
        } catch (Exception e) {
            Mundo.debug(MineSkinClient.class, e);
            return null;
        }
    }

    public static String rawStringFromFile(File file) {
        try {
            Connection connection = HttpConnection
                    .connect(String.format(UPLOAD_FORMAT, SKIN_OPTIONS))
                    .userAgent(USER_AGENT)
                    .method(Connection.Method.POST)
                    .data("file", file.getName(), new FileInputStream(file))
                    .ignoreContentType(true)
                    .timeout(10000);
            return connection.execute().body();
        } catch (Exception e) {
            Mundo.debug(MineSkinClient.class, e);
            return null;
        }
    }

    public static Skin fromRawString(String string) {
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(string);
            JSONObject subJSON = (JSONObject) (
                    (JSONObject) jsonObject.get("data")
            ).get("texture");
            Skin skin = new Skin.Simple(
                    (String) subJSON.get("value"),
                    (String) subJSON.get("signature"));
            return skin;
        } catch (ParseException | ClassCastException e) {
            Mundo.debug(MineSkinClient.class, e);
            return null;
        }
    }

}