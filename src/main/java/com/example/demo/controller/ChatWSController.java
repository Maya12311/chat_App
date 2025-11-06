package com.example.demo.controller;


import com.example.demo.dto.ChatMessageDto;
import com.example.demo.model.ChatMessage;
import com.example.demo.repository.ChatMessageRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;

@Controller
public class ChatWSController {

    private final SimpMessagingTemplate broker;
    private final ChatMessageRepository messages;

    public ChatWSController(SimpMessagingTemplate broker, ChatMessageRepository messages) {
        this.broker = broker;
        this.messages = messages;
    }

    @MessageMapping("/chat.send") // Client publish -> /app/chat.send
    public void onSend(ChatMessageDto inbound) {
        // persistieren
        ChatMessage m = new ChatMessage();
        m.setSenderId(inbound.senderId);
        m.setRecipientId(inbound.recipientId);
        m.setGroupCode(inbound.groupCode);
        m.setContent(inbound.content);
        m.setClientId(inbound.clientId);
        m.setTimestamp(Instant.now());
        m = messages.save(m);

        // Outbound DTO
        ChatMessageDto out = new ChatMessageDto();
        out.id = m.getId();
        out.senderId = m.getSenderId();
        out.recipientId = m.getRecipientId();
        out.groupCode = m.getGroupCode();
        out.content = m.getContent();
        out.timestamp = m.getTimestamp();
        out.readByRecipient = m.isReadByRecipient();
        out.clientId = m.getClientId();

        if (out.groupCode != null) {
            broker.convertAndSend("/topic/group-" + out.groupCode, out);
        } else if (out.recipientId != null) {
            broker.convertAndSendToUser(out.recipientId, "/queue/messages", out);
            broker.convertAndSendToUser(out.senderId, "/queue/messages", out); // echo an Sender
        }
    }
}
