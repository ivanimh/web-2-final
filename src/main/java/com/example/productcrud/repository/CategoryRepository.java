package com.example.productcrud.repository;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByOwner(User owner);

    Optional<Category> findByIdAndOwner(Long id, User owner);

    boolean existsByNameAndOwner(String name, User owner);

    // =====================================================
    // SEARCH — filter nama kategori (partial, case-insensitive)
    // Jika keyword null/blank -> kembalikan semua milik owner
    // CAST fix agar PostgreSQL tidak baca null sebagai bytea
    // =====================================================
    @Query("SELECT c FROM Category c " +
            "WHERE c.owner = :owner " +
            "AND (CAST(:keyword AS string) IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) " +
            "ORDER BY c.name ASC")
    List<Category> searchByOwner(
            @Param("owner")   User owner,
            @Param("keyword") String keyword);
}