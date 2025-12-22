package ru.yandex.practicum.config;

import java.security.SecureRandom;
import java.util.Base64;

public class SecretGenerator {
    public static String generate() {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[32];
        random.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }
}
