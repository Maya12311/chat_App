package com.example.demo.repository;

import com.example.demo.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // ----- DM: Seite holen (vor einem Zeitpunkt), DESC für effizientes LIMIT; Client kann danach umdrehen
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE (
            (m.senderId = :a AND m.recipientId = :b)
         OR (m.senderId = :b AND m.recipientId = :a)
        )
        AND m.timestamp < :before
        ORDER BY m.timestamp DESC
    """)
    List<ChatMessage> dmPage(@Param("a") String a,
                             @Param("b") String b,
                             @Param("before") Instant before,
                             Pageable pageable);

    // ----- Gruppe: Seite holen (vor einem Zeitpunkt)
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE m.groupCode = :code AND m.timestamp < :before
        ORDER BY m.timestamp DESC
    """)
    List<ChatMessage> groupPage(@Param("code") String code,
                                @Param("before") Instant before,
                                Pageable pageable);

    // ----- Unread Counts (bleiben wie gehabt)
    long countByRecipientIdAndSenderIdAndReadByRecipientFalse(String recipientId, String senderId);
    long countByGroupCode(String code);
    long countByGroupCodeAndTimestampAfter(String code, Instant since);

    // Für initiale Verläufe ohne before kannst du "vor jetzt" nutzen – macht der Service.
}
