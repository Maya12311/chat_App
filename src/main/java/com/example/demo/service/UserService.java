package com.example.demo.service;


import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository users;
    private final SimpMessagingTemplate template;

    public UserService(UserRepository users, SimpMessagingTemplate template) {
        this.users = users; this.template = template;
    }

    public User upsertOnline(String nick, String full, boolean online) {
        User u = users.findByNickName(nick).orElseGet(User::new);
        u.setNickName(nick);
        u.setFullName(full);
        u.setOnline(online);
        User saved = users.save(u);
        template.convertAndSend("/topic/users", list());
        return saved;
    }

    public List<User> list() { return users.findAll(); }
}



