package ru.yandex.practicum.service;
import ru.yandex.practicum.config.ConfigStorage;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ServiceHMAC {

    private final Mac mac;

    public ServiceHMAC(ConfigStorage.ConfigHMAC config, PrintWriter log) throws NoSuchAlgorithmException, InvalidKeyException {
        mac = Mac.getInstance(config.getHmacAlg());
        mac.init(new SecretKeySpec(config.getSecret().getBytes(StandardCharsets.UTF_8), config.getHmacAlg()));
    }

    public byte[] sign(byte[] msg) {
        if (msg == null || msg.length == 0) {
            throw new IllegalArgumentException("msg cannot be null or empty");
        }
        byte[] sig = mac.doFinal(msg);
        return sig;
    }

    public boolean verify(byte[] msg, byte[] signature) {
        byte[] sig = mac.doFinal(msg);
        return MessageDigest.isEqual(sig, signature);
    }
}
