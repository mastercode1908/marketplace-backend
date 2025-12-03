    package com.group7.marketplacesystem.catalog.repository;
    import com.group7.marketplacesystem.catalog.entity.Category;
    import org.springframework.data.domain.Page;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.stereotype.Repository;
    import org.springframework.data.domain.Pageable;


    @Repository
    public interface CategoryRepository extends JpaRepository<Category, Integer> {

        @Query(
                value = "SELECT * FROM category c " +
                        "WHERE (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "AND c.deleted_at IS NULL",
                countQuery = "SELECT COUNT(*) FROM category c " +
                        "WHERE (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "AND c.deleted_at IS NULL",
                nativeQuery = true
        )
        Page<Category> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
    }

