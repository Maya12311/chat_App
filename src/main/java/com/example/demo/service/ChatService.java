package com.example.demo.service;

import com.example.demo.model.ChatMessage;
import com.example.demo.repository.ChatMessageRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ChatService {
    private final ChatMessageRepository messages;
    private final SimpMessagingTemplate template;

    public ChatService(ChatMessageRepository messages, SimpMessagingTemplate template) {
        this.messages = messages;
        this.template = template;
    }

    // ===== Senden: Privat =====
    @Transactional
    public ChatMessage sendPrivate(ChatMessage m) {
        m.setTimestamp(Instant.now());
        m.setReadByRecipient(false);
        ChatMessage saved = messages.save(m);

        // an Empfänger …
        template.convertAndSendToUser(saved.getRecipientId(), "/queue/messages", saved);
        // … und Echo an Sender
        template.convertAndSendToUser(saved.getSenderId(), "/queue/messages", saved);
        return saved;
    }

    // ===== Senden: Gruppe =====
    @Transactional
    public ChatMessage sendGroup(ChatMessage m) {
        m.setTimestamp(Instant.now());
        m.setRecipientId(null);       // in Gruppen gibt's keinen recipient
        m.setReadByRecipient(false);  // Flag wird nur für DMs genutzt
        ChatMessage saved = messages.save(m);

        template.convertAndSend("/topic/group-" + saved.getGroupCode(), saved);
        return saved;
    }

    // ===== Verlauf: Privat =====
    @Transactional(readOnly = true)
    public List<ChatMessage> historyPrivate(String a, String b) {
        // (a->b) ODER (b->a) – chronologisch
        return messages.findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByTimestampAsc(a, b, b, a);
    }

    // ===== Verlauf: Gruppe =====
    @Transactional(readOnly = true)
    public List<ChatMessage> historyGroup(String code) {
        return messages.findByGroupCodeOrderByTimestampAsc(code);
    }

    // ===== Als gelesen markieren (DM) =====
    @Transactional
    public void markReadForPair(String recipient, String sender) {
        List<ChatMessage> list = historyPrivate(sender, recipient);
        list.forEach(m -> {
            if (!m.isReadByRecipient() && recipient.equals(m.getRecipientId())) {
                m.setReadByRecipient(true);
            }
        });
        messages.saveAll(list); // einmalig speichern
    }

    // ===== Ungelesene zählen (DM) =====
    @Transactional(readOnly = true)
    public long unreadCount(String recipient, String sender) {
        return messages.countByRecipientIdAndSenderIdAndReadByRecipientFalse(recipient, sender);
    }
}
