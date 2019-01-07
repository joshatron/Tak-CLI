package io.joshatron.tak.cli.app.server;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ServerConfig {

    private String configFile;

    private String serverUrl;
    private String username;

    public ServerConfig() {
        configFile = "server-config.json";
        importConfig();
    }

    public ServerConfig(String file) {
        configFile = file;
        importConfig();
    }

    public void importConfig() {
        try {
            String input = FileUtils.readFileToString(new File(configFile), Charset.defaultCharset());
            JSONObject json = new JSONObject(input);
            serverUrl = json.getString("serverUrl");
            username = json.getString("username");
        } catch (IOException e) {
            System.out.println("Could not find server config, creating new one.");
        }
    }

    public void exportConfig() {
        JSONObject json = new JSONObject();
        if(serverUrl != null) {
            json.put("serverUrl", serverUrl);
        }
        if(username != null) {
            json.put("username", username);
        }

        try {
            FileUtils.writeStringToFile(new File(configFile), json.toString(4), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
