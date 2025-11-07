package com.example.demo.service;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.GroupRead;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.GroupReadRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ChatService {

    private final ChatMessageRepository messages;
    private final GroupReadRepository groupReads;
    private final SimpMessagingTemplate template;

    public ChatService(ChatMessageRepository messages,
                       GroupReadRepository groupReads,
                       SimpMessagingTemplate template) {
        this.messages = messages;
        this.groupReads = groupReads;
        this.template = template;
    }

    // ===== Senden =====

    @Transactional
    public ChatMessage sendPrivate(ChatMessage m) {
        m.setTimestamp(Instant.now());
        m.setReadByRecipient(false);
        ChatMessage saved = messages.save(m);
        // Echo an Sender + Empfänger per Topic
        template.convertAndSend("/topic/user-" + saved.getRecipientId(), saved);
        template.convertAndSend("/topic/user-" + saved.getSenderId(), saved);
        return saved;
    }

    @Transactional
    public ChatMessage sendGroup(ChatMessage m) {
        m.setTimestamp(Instant.now());
        m.setRecipientId(null);
        m.setReadByRecipient(false);
        ChatMessage saved = messages.save(m);
        template.convertAndSend("/topic/group-" + saved.getGroupCode(), saved);
        return saved;
    }

    // ===== Historie (Paging) =====

    @Transactional(readOnly = true)
    public List<ChatMessage> historyPrivatePage(String a, String b, Instant before, int limit) {
        if (before == null) before = Instant.now();
        var page = PageRequest.of(0, Math.max(1, Math.min(limit, 100)));
        var list = messages.dmPage(a, b, before, page);
        // Repository gibt DESC – für UI angenehmer ASC
        List<ChatMessage> asc = new ArrayList<>(list);
        Collections.reverse(asc);
        return asc;
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> historyGroupPage(String code, Instant before, int limit) {
        if (before == null) before = Instant.now();
        var page = PageRequest.of(0, Math.max(1, Math.min(limit, 100)));
        var list = messages.groupPage(code, before, page);
        List<ChatMessage> asc = new ArrayList<>(list);
        Collections.reverse(asc);
        return asc;
    }

    // ===== Read/Unread (DM) =====

    @Transactional
    public void markReadForPair(String recipient, String sender) {
        // Markiere alle an "recipient" gerichteten Nachrichten von "sender" als gelesen (bis jetzt)
        var page = historyPrivatePage(sender, recipient, Instant.now(), 500); // simple: große Menge
        boolean changed = false;
        for (var m : page) {
            if (!m.isReadByRecipient() && recipient.equals(m.getRecipientId())) {
                m.setReadByRecipient(true);
                changed = true;
            }
        }
        if (changed) messages.saveAll(page);
    }

    @Transactional(readOnly = true)
    public long unreadCount(String recipient, String sender) {
        return messages.countByRecipientIdAndSenderIdAndReadByRecipientFalse(recipient, sender);
    }

    // ===== Read/Unread (Group) =====

    @Transactional
    public void markGroupRead(String code, String user) {
        var id = new GroupRead.Id(user, code);
        var now = Instant.now();
        var gr = groupReads.findById(id).orElse(new GroupRead(user, code, now));
        gr.setLastReadAt(now);
        groupReads.save(gr);
    }

    @Transactional(readOnly = true)
    public long groupUnread(String code, String user) {
        var id = new GroupRead.Id(user, code);
        var gr = groupReads.findById(id).orElse(null);
        if (gr == null || gr.getLastReadAt() == null) {
            return messages.countByGroupCode(code);
        }
        return messages.countByGroupCodeAndTimestampAfter(code, gr.getLastReadAt());
    }
}
