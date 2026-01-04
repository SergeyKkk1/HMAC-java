package ru.yandex.practicum;

import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.config.ConfigStorage;
import ru.yandex.practicum.config.SecretGenerator;
import ru.yandex.practicum.service.ServiceHMAC;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class ServerHMAC {

    private final ConfigStorage configStorage;

    private final ConfigStorage.ConfigHMAC configHMAC;
    private final PrintWriter log;
    private HttpServer server;

    public ServerHMAC(ConfigStorage configStorage, PrintWriter log) throws IOException {
        this.configStorage = configStorage;
        this.configHMAC = configStorage.load();
        this.log = log;
    }

    public static void main(String[] args) {
        try (FileOutputStream fos = new FileOutputStream("log.txt"); Writer writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            PrintWriter log = new PrintWriter(writer, true);
            //log = new PrintWriter(System.out, true); //TODO comment on commit
            try {
                ConfigStorage config = new ConfigStorage(log);
                ServerHMAC serverHMAC = new ServerHMAC(config, log);
                serverHMAC.run();
                serverHMAC.runCommandLoop();
            } catch (Exception e) {
                e.printStackTrace(log);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        server = HttpServer.create();
        ServiceHMAC service = new ServiceHMAC(configHMAC);
        server.bind(new InetSocketAddress(configHMAC.getListenPort()), 0);
        server.createContext("/sign", new HandlerSignHMAC(service, configHMAC, log));
        server.createContext("/verify", new HandlerVerifyHMAC(service, configHMAC, log));
        server.start();
        log.println("HMAC Server Started");
    }

    public void runCommandLoop() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Do you want to rotate secret? (y/n)");
        boolean isRunning = true;
        while (isRunning) {
            if (scanner.hasNextLine()) {
                String command = scanner.nextLine().trim().toLowerCase();

                switch (command) {
                    case "y":
                        secretRotation();
                        break;
                    case "n":
                    case "exit":
                        isRunning = false;
                        scanner.close();
                        break;
                    default:
                        log.println("Unknown command");
                }
            }
        }
    }

    public ConfigStorage.ConfigHMAC getConfigHMAC() {
        return configHMAC;
    }

    private void secretRotation() {
        String newSecret = SecretGenerator.generate();
        configHMAC.setSecret(newSecret);
        System.out.println("New secret: " + newSecret);
        try {
            configStorage.store(configHMAC);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        server.stop(0);
    }
}
