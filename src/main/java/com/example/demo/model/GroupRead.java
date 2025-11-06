package com.example.demo.model;


import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "group_reads")
public class GroupRead {

    @EmbeddedId
    private Id id;

    private Instant lastReadAt;

    public GroupRead() {}
    public GroupRead(String userId, String groupCode, Instant lastReadAt) {
        this.id = new Id(userId, groupCode);
        this.lastReadAt = lastReadAt;
    }

    public Id getId() { return id; }
    public void setId(Id id) { this.id = id; }
    public Instant getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(Instant lastReadAt) { this.lastReadAt = lastReadAt; }

    @Embeddable
    public static class Id implements Serializable {
        private String userId;
        private String groupCode;

        public Id() {}
        public Id(String userId, String groupCode) { this.userId = userId; this.groupCode = groupCode; }

        public String getUserId() { return userId; }
        public String getGroupCode() { return groupCode; }
        public void setUserId(String userId) { this.userId = userId; }
        public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id id1)) return false;
            return Objects.equals(userId, id1.userId) && Objects.equals(groupCode, id1.groupCode);
        }
        @Override public int hashCode() { return Objects.hash(userId, groupCode); }
    }
}
