package com.example.demo.ws;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class NickHeaderInterceptor implements ChannelInterceptor {

    // mappt WebSocket-SessionId -> Nickname
    public static final ConcurrentHashMap<String, String> SESSION_NICK = new ConcurrentHashMap<>();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            String nick = acc.getFirstNativeHeader("nick");
            if (nick != null && acc.getSessionId() != null) {
                SESSION_NICK.put(acc.getSessionId(), nick);
            }
        }

        if (StompCommand.DISCONNECT.equals(acc.getCommand())) {
            if (acc.getSessionId() != null) {
                SESSION_NICK.remove(acc.getSessionId());
            }
        }
        return message;
    }
}
