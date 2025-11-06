package com.example.demo.model;


import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "nick_name", length = 64)
    private String nickName;


    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private boolean online = false;

    @Column(name = "last_seen")
    private Instant lastSeen;

    // --- Getter/Setter ---
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public Instant getLastSeen() { return lastSeen; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }
}
