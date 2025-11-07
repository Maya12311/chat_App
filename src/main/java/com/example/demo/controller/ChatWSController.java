package com.example.demo.controller;

import com.example.demo.model.ChatMessage;
import com.example.demo.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWSController {

    private final ChatService chat;

    public ChatWSController(ChatService chat) {
        this.chat = chat;
    }

    @MessageMapping("/chat.send")
    public void onSend(ChatMessage inbound) {
        // Wenn groupCode gesetzt -> Gruppe; sonst DM
        if (inbound.getGroupCode() != null && !inbound.getGroupCode().isEmpty()) {
            chat.sendGroup(inbound);
        } else {
            chat.sendPrivate(inbound);
        }
    }
}
