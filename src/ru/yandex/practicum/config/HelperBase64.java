package ru.yandex.practicum.config;

import java.util.Base64;

public class HelperBase64 {

    public static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }
}
