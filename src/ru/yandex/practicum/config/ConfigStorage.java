package ru.yandex.practicum.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ConfigStorage {

    private static final String CONFIG_FILE = "config.json";

    private final PrintWriter log;

    public ConfigStorage(PrintWriter log) {
        this.log = log;
    }

    public ConfigHMAC load() throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        try (FileInputStream fis = new FileInputStream(CONFIG_FILE);
             BufferedReader in = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            ConfigHMAC configHMAC = gson.fromJson(in, new ConfigHMACTypeToken().getType());
            log.println("Loaded config from file: " + CONFIG_FILE);
            return configHMAC;
        }
    }

    public void store(ConfigHMAC config) throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8))) {
            gson.toJson(config, out);
            log.println("Config file updated: " + CONFIG_FILE);
        }
    }

    public static class ConfigHMAC {
        private String hmacAlg;
        private String secret;
        private Integer maxMsgSizeBytes = 1048576;
        private Integer listenPort = 8080;

        public String getHmacAlg() {
            return hmacAlg;
        }

        public void setHmacAlg(String hmacAlg) {
            this.hmacAlg = hmacAlg;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Integer getMaxMsgSizeBytes() {
            return maxMsgSizeBytes;
        }

        public void setMaxMsgSizeBytes(Integer maxMsgSizeBytes) {
            this.maxMsgSizeBytes = maxMsgSizeBytes;
        }

        public Integer getListenPort() {
            return listenPort;
        }

        public void setListenPort(Integer listenPort) {
            this.listenPort = listenPort;
        }
    }

    public class ConfigHMACTypeToken extends TypeToken<ConfigHMAC> {
    }
}


