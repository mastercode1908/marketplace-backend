package com.group7.marketplacesystem.chatbotRAG.repository;

import com.group7.marketplacesystem.chatbotRAG.entity.ProductVector;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProductVector entity.
 * Uses PostgreSQL with pgvector extension for vector similarity search.
 */
@Repository
public interface ProductVectorRepository extends JpaRepository<ProductVector, Integer> {

        /**
         * Find product vector by product ID from MySQL.
         */
        Optional<ProductVector> findByProductId(Integer productId);

        /**
         * Delete product vector by product ID.
         */
        @Transactional
        @Modifying
        @Query(value = "DELETE FROM product_vector WHERE product_id = :productId", nativeQuery = true)
        void deleteByProductId(@Param("productId") Integer productId);

        /**
         * Check if product vector exists for given product ID.
         */
        @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM product_vector WHERE product_id = :productId", nativeQuery = true)
        boolean existsByProductId(@Param("productId") Integer productId);

        /**
         * Insert or update product vector with CAST to vector type.
         */
        @Transactional
        @Modifying
        @Query(value = "INSERT INTO product_vector (product_id, name, description, embedding, created_at) " +
                        "VALUES (:productId, :name, :description, CAST(:embedding AS vector), CURRENT_TIMESTAMP) " +
                        "ON CONFLICT (product_id) DO UPDATE SET " +
                        "name = :name, description = :description, embedding = CAST(:embedding AS vector)", nativeQuery = true)
        void upsertProductVector(@Param("productId") Integer productId,
                        @Param("name") String name,
                        @Param("description") String description,
                        @Param("embedding") String embedding);

        /**
         * Find top K most similar products using cosine distance.
         * Uses pgvector's <=> operator for cosine distance.
         * Lower distance = more similar.
         * 
         * @param embeddingVector Vector embedding as string "[0.1,0.2,...]"
         * @param limit           Maximum number of results
         * @return List of similar product vectors ordered by similarity (most similar
         *         first)
         */
        @Query(value = "SELECT * FROM product_vector " +
                        "ORDER BY embedding <=> CAST(:embedding AS vector) " +
                        "LIMIT :limit", nativeQuery = true)
        List<ProductVector> findTopKSimilar(
                        @Param("embedding") String embeddingVector,
                        @Param("limit") int limit);

        /**
         * Find top K most similar products excluding a specific product ID.
         * Useful for "similar products" recommendations.
         */
        @Query(value = "SELECT * FROM product_vector " +
                        "WHERE product_id != :excludeProductId " +
                        "ORDER BY embedding <=> CAST(:embedding AS vector) " +
                        "LIMIT :limit", nativeQuery = true)
        List<ProductVector> findTopKSimilarExcluding(
                        @Param("embedding") String embeddingVector,
                        @Param("excludeProductId") Integer productId,
                        @Param("limit") int limit);

        /**
         * Count total vectors in database.
         */
        @Query("SELECT COUNT(pv) FROM ProductVector pv")
        long countAll();
}
