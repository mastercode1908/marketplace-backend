package com.group7.marketplacesystem.communication.entity;

import com.group7.marketplacesystem.identity.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "conversation", indexes = {
        @Index(name = "idx_buyer", columnList = "buyer_id"),
        @Index(name = "idx_seller", columnList = "seller_id"),
        @Index(name = "idx_lastmsg", columnList = "last_message_time")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uniq_buyer_seller", columnNames = {"buyer_id", "seller_id"})
})
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Lob
    @Column(name = "last_message")
    private String lastMessage;

    @Column(name = "last_message_time")
    private Instant lastMessageTime;

    @ColumnDefault("0")
    @Column(name = "unread_count_buyer")
    private Integer unreadCountBuyer;

    @ColumnDefault("0")
    @Column(name = "unread_count_seller")
    private Integer unreadCountSeller;

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

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

}