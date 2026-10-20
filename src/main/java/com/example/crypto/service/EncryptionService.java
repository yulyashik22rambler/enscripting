package com.example.crypto.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // Рекомендуемая длина IV для GCM
    private static final int GCM_TAG_LENGTH = 128; // Длина тега аутентификации в битах

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    // Ключ берется строго из переменных окружения
    public EncryptionService(@Value("${DB_ENCRYPTION_KEY}") String base64Key) {
        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalStateException("Критическая ошибка: Переменная окружения DB_ENCRYPTION_KEY не задана!");
        }
        try {
            byte[] decodedKey = Base64.getDecoder().decode(base64Key);
            if (decodedKey.length != 32) { // 32 байта = 256 бит
                throw new IllegalArgumentException("Длина ключа AES должна быть строго 256 бит (32 байта).");
            }
            this.secretKey = new SecretKeySpec(decodedKey, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации ключа шифрования: " + e.getMessage(), e);
        }
    }

    public String encrypt(String data) {
        if (data == null) return null;
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv); // Генерируем уникальный IV

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Объединяем IV и зашифрованный текст в один массив для удобства хранения
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при шифровании данных: " + e.getMessage(), e);
        }
    }

    public String decrypt(String encryptedData) {
        if (encryptedData == null) return null;
        try {
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);

            if (decodedData.length < GCM_IV_LENGTH) {
                throw new IllegalArgumentException("Зашифрованные данные повреждены или имеют неверный формат.");
            }

            // Извлекаем IV из начала массива
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decodedData, 0, iv, 0, iv.length);

            // Извлекаем непосредственно зашифрованный текст
            int cipherTextLength = decodedData.length - GCM_IV_LENGTH;
            byte[] cipherText = new byte[cipherTextLength];
            System.arraycopy(decodedData, iv.length, cipherText, 0, cipherTextLength);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при дешифровании данных (возможно, неверный ключ): " + e.getMessage(), e);
        }
    }
}
