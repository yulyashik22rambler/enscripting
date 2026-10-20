package com.example.crypto.controller;
import com.example.crypto.entity.UserCard;
import com.example.crypto.repository.UserCardRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final UserCardRepository repository;

    public CardController(UserCardRepository repository) {
        this.repository = repository;
    }

    // Сохранение карты (автоматически зашифрует в БД)
    @PostMapping
    public UserCard createCard(@RequestBody UserCard card) {
        return repository.save(card);
    }

    // Получение всех карт (автоматически расшифрует при выводе)
    @GetMapping
    public List<UserCard> getAllCards() {
        return repository.findAll();
    }
}