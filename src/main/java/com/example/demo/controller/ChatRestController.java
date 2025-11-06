package com.example.demo.controller;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.User;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ChatService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatRestController {

    private final UserRepository users;
    private final ChatMessageRepository messages;
    private final ChatService chatService;
    private final SimpMessagingTemplate broker;

    public ChatRestController(UserRepository users, ChatMessageRepository messages,
                              ChatService chatService, SimpMessagingTemplate broker) {
        this.users = users;
        this.messages = messages;
        this.chatService = chatService;
        this.broker = broker;
    }

    // 🔹 Liste aller Benutzer
    @GetMapping("/users")
    public List<User> allUsers() {
        return users.findAll();
    }

    // 🔹 Benutzerstatus (online/offline)
    @PostMapping("/user/status")
    public void status(@RequestParam String nick,
                       @RequestParam String full,
                       @RequestParam boolean online) {
        User u = users.findByNickName(nick).orElseGet(User::new);
        u.setNickName(nick);
        u.setFullName(full);
        u.setOnline(online);
        u.setLastSeen(Instant.now());
        users.save(u);
        broker.convertAndSend("/topic/users", users.findAll());
    }

    // 🔹 Private Nachrichtenverlauf
    @GetMapping("/messages/private/{a}/{b}")
    public List<ChatMessage> privateHistory(@PathVariable String a, @PathVariable String b) {
        return chatService.historyPrivate(a, b);
    }

    // 🔹 Gruppennachrichtenverlauf
    @GetMapping("/groups/{code}/messages")
    public List<ChatMessage> groupHistory(@PathVariable String code) {
        return chatService.historyGroup(code);
    }

    // 🔹 Nachrichten als gelesen markieren (z. B. beim Öffnen des Chats)
    @PostMapping("/messages/markRead")
    public void markRead(@RequestParam String recipient, @RequestParam String sender) {
        chatService.markReadForPair(recipient, sender);
    }

    // 🔹 Zähle ungelesene Nachrichten eines Senders an einen Empfänger
    @GetMapping("/unreadCount")
    public long unreadCount(@RequestParam String recipient, @RequestParam String sender) {
        return chatService.unreadCount(recipient, sender);
    }
}
