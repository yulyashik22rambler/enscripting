package com.example.crypto.entity;

import com.example.crypto.converter.EncryptedStringConverter;
import jakarta.persistence.*;

@Entity
@Table(name = "user_cards")
public class UserCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    // Конструкторы, геттеры и сеттеры
    public UserCard() {}

    public UserCard(String username, String cardNumber) {
        this.username = username;
        this.cardNumber = cardNumber;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
}