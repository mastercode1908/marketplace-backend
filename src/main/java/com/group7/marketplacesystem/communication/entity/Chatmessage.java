package com.group7.marketplacesystem.communication.entity;

import com.group7.marketplacesystem.identity.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "chatmessage", indexes = {
        @Index(name = "idx_conv_time", columnList = "conversation_id, sent_at")
})
public class Chatmessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ColumnDefault("'TEXT'")
    @Column(name = "message_type")
    private String messageType;

    @Lob
    @Column(name = "content")
    private String content;

    @Size(max = 1000)
    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "sent_at")
    private Instant sentAt;

    @ColumnDefault("0")
    @Column(name = "is_read")
    private Boolean isRead;

    @ColumnDefault("0")
    @Column(name = "is_deleted_buyer")
    private Boolean isDeletedBuyer;

    @Column(name = "deleted_at_buyer")
    private Instant deletedAtBuyer;

    @ColumnDefault("0")
    @Column(name = "is_deleted_seller")
    private Boolean isDeletedSeller;

    @Column(name = "deleted_at_seller")
    private Instant deletedAtSeller;

}