package com.example.demo.controller;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.User;
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
    private final ChatService chat;
    private final SimpMessagingTemplate broker;

    public ChatRestController(UserRepository users, ChatService chat, SimpMessagingTemplate broker) {
        this.users = users;
        this.chat = chat;
        this.broker = broker;
    }

    @GetMapping("/users")
    public List<User> allUsers() { return users.findAll(); }

    @PostMapping("/user/status")
    public void status(@RequestParam String nick, @RequestParam String full, @RequestParam boolean online) {
        var u = users.findById(nick).orElseGet(User::new);
        u.setNickName(nick);
        u.setFullName(full);
        u.setOnline(online);
        u.setLastSeen(Instant.now());
        users.save(u);
        broker.convertAndSend("/topic/users", "refresh");
    }

    // ----- DM Verlauf (Paging)
    @GetMapping("/messages/private/{a}/{b}")
    public List<ChatMessage> privateHistory(@PathVariable String a,
                                            @PathVariable String b,
                                            @RequestParam(required = false) String before,
                                            @RequestParam(defaultValue = "30") int limit) {
        Instant t = (before == null || before.isBlank()) ? null : Instant.parse(before);
        return chat.historyPrivatePage(a, b, t, limit);
    }

    @PostMapping("/messages/markRead")
    public void markRead(@RequestParam String recipient, @RequestParam String sender) {
        chat.markReadForPair(recipient, sender);
    }

    @GetMapping("/unreadCount")
    public long unreadCount(@RequestParam String recipient, @RequestParam String sender) {
        return chat.unreadCount(recipient, sender);
    }

    // ----- Group Verlauf (Paging)
    @GetMapping("/groups/{code}/messages")
    public List<ChatMessage> groupHistory(@PathVariable String code,
                                          @RequestParam(required = false) String before,
                                          @RequestParam(defaultValue = "30") int limit) {
        Instant t = (before == null || before.isBlank()) ? null : Instant.parse(before);
        return chat.historyGroupPage(code, t, limit);
    }

    @PostMapping("/groups/{code}/markRead")
    public void groupMarkRead(@PathVariable String code, @RequestParam String user) {
        chat.markGroupRead(code, user);
    }

    @GetMapping("/groups/{code}/unread")
    public long groupUnread(@PathVariable String code, @RequestParam String user) {
        return chat.groupUnread(code, user);
    }
}
