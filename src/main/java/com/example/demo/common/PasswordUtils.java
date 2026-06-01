package com.example.demo.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// 教学项目里使用 SHA-256 做简单密码摘要，真实项目更推荐 BCrypt。
public final class PasswordUtils {
    private PasswordUtils() {
    }

    public static String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte value : bytes) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        return hash(rawPassword).equals(hashedPassword);
    }
}
