package com.group7.marketplacesystem.infrastructure.entity;

import com.group7.marketplacesystem.identity.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "fileupload", indexes = {
        @Index(name = "idx_fileuser", columnList = "user_id"),
        @Index(name = "idx_file_type", columnList = "file_type")
})
public class Fileupload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl;

    @Column(name = "file_type", nullable = false, length = 100)
    private String fileType;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

}