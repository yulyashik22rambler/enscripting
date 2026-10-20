package com.example.crypto;

import com.example.crypto.entity.UserCard;
import com.example.crypto.repository.UserCardRepository;
import com.example.crypto.service.EncryptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CryptoServiceApplicationTests {

    static {
        // Генерируем случайный валидный 256-битный ключ для тестового окружения
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        String base64Key = Base64.getEncoder().encodeToString(key);

        // Устанавливаем системное свойство, которое Spring подставит вместо переменной окружения
        System.setProperty("DB_ENCRYPTION_KEY", base64Key);
    }

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private UserCardRepository userCardRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testDirectEncryptionAndDecryption() {
        String originalData = "4242-4242-4242-4242";

        String encrypted = encryptionService.encrypt(originalData);
        assertNotEquals(originalData, encrypted);

        String decrypted = encryptionService.decrypt(encrypted);
        assertEquals(originalData, decrypted);
    }

    @Test
    void testAutomaticDbEncryption() {
        String rawCard = "1111-2222-3333-4444";
        UserCard card = new UserCard("JohnDoe", rawCard);

        // Сохраняем в БД через JPA
        UserCard savedCard = userCardRepository.save(card);

        // 1. Проверяем сырой SQL: в БД данные обязаны быть зашифрованы
        String encryptedCardInDb = jdbcTemplate.queryForObject(
                "SELECT card_number FROM user_cards WHERE id = ?",
                String.class,
                savedCard.getId()
        );
        assertNotEquals(rawCard, encryptedCardInDb);
        assertEquals(rawCard, encryptionService.decrypt(encryptedCardInDb));

        // 2. Проверяем авто-дешифрование: при выгрузке через JPA данные должны вернуться в открытом виде
        UserCard fetchedCard = userCardRepository.findById(savedCard.getId()).orElseThrow();
        assertEquals(rawCard, fetchedCard.getCardNumber());
    }

    @Test
    void testDecryptionErrorHandling() {
        // Передача поврежденной/некорректной строки должна вызывать контролируемое исключение
        assertThrows(RuntimeException.class, () -> encryptionService.decrypt("not-a-valid-base64-string"));
    }
}