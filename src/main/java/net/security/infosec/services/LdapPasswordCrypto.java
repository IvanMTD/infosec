package net.security.infosec.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
public class LdapPasswordCrypto {

    private final TextEncryptor encryptor;

    public LdapPasswordCrypto(@Value("${jwt.secret:default}") String secret) {
        // Используем jwt.secret как ключ + соль
        String salt = "deadbeef";
        this.encryptor = Encryptors.text(secret, salt);
    }

    public String encrypt(String plain) {
        if (plain == null || plain.isBlank()) return "";
        return encryptor.encrypt(plain);
    }

    public String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isBlank()) return "";
        try {
            return encryptor.decrypt(encrypted);
        } catch (Exception e) {
            // Если не удалось расшифровать (старый открытый пароль) — возвращаем как есть
            return encrypted;
        }
    }
}
