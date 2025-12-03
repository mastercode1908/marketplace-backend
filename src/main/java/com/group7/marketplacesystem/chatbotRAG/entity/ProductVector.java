package com.group7.marketplacesystem.chatbotRAG.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Entity representing a product with its vector embedding for RAG
 * (Retrieval-Augmented Generation).
 * Stored in PostgreSQL with pgvector extension.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_vector")
public class ProductVector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "product_id", nullable = false, unique = true)
    private Integer productId;

    @Column(name = "name", length = Integer.MAX_VALUE)
    private String name;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    /**
     * Vector embedding of the product (name + description).
     * Uses pgvector type with dimension 768 (Gemini
     * text-embedding-004).
     * Stored as TEXT representation of float array in PostgreSQL.
     */
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private String embedding;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /**
     * Convert float array to pgvector string format: "[0.1,0.2,0.3,...]"
     */
    public void setEmbeddingFromFloats(float[] floats) {
        if (floats == null) {
            this.embedding = null;
            return;
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < floats.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(floats[i]);
        }
        sb.append("]");
        this.embedding = sb.toString();
    }

    /**
     * Convert double array to pgvector string format: "[0.1,0.2,0.3,...]"
     */
    public void setEmbeddingFromDoubles(double[] doubles) {
        if (doubles == null) {
            this.embedding = null;
            return;
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < doubles.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(doubles[i]);
        }
        sb.append("]");
        this.embedding = sb.toString();
    }
}