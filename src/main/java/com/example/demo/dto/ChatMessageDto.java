package com.example.demo.dto;

import java.time.Instant;

public class ChatMessageDto {
    public Long id;
    public String senderId;
    public String recipientId; // null bei Gruppe
    public String groupCode;   // null bei DM
    public String content;
    public Instant timestamp;
    public Boolean readByRecipient;
    public String clientId;
}
