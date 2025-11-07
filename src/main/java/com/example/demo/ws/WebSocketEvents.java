package com.example.demo.ws;


import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class WebSocketEvents {

    private final UserRepository users;
    private final SimpMessagingTemplate broker;

    public WebSocketEvents(UserRepository users, SimpMessagingTemplate broker) {
        this.users = users;
        this.broker = broker;
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent e) {
        var sessionId = e.getSessionId();
        var nick = NickHeaderInterceptor.SESSION_NICK.remove(sessionId);
        if (nick == null) return;

        users.findById(nick).ifPresent(u -> {
            u.setOnline(false);
            u.setLastSeen(Instant.now());
            users.save(u);
            // alle Clients: Userliste aktualisieren
            broker.convertAndSend("/topic/users", "refresh");
        });
    }
}
