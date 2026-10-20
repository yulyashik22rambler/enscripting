package com.example.crypto.converter;

import com.example.crypto.service.EncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.context.annotation.Lazy;

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private final EncryptionService encryptionService;

    // Используем @Lazy, чтобы избежать циклической зависимости при инициализации JPA контекста
    public EncryptedStringConverter(@Lazy EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        // Срабатывает автоматически ПЕРЕД сохранением в БД
        return encryptionService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        // Срабатывает автоматически ПОСЛЕ чтения из БД
        return encryptionService.decrypt(dbData);
    }
}