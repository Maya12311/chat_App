package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user") // <— NICHT "/api"
public class UserController {

    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    // Einzelnen User holen: GET /api/user/{nick}
    @GetMapping("/{nick}")
    public ResponseEntity<User> get(@PathVariable String nick) {
        return users.findById(nick)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // WICHTIG: KEINE Methode mit @GetMapping("/users") hier definieren!
}
