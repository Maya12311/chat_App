package com.example.demo.repository;


import com.example.demo.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // DM-Verlauf: (A -> B) ODER (B -> A), chronologisch
    List<ChatMessage> findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByTimestampAsc(
            String senderA, String recipientA,
            String senderB, String recipientB
    );

    // Gruppenverlauf chronologisch
    List<ChatMessage> findByGroupCodeOrderByTimestampAsc(String groupCode);

    // Ungelesene DMs zählen
    long countByRecipientIdAndSenderIdAndReadByRecipientFalse(String recipientId, String senderId);
}
