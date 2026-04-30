package com.example.productcrud.repository;

import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByOwner_Id(Long ownerId, Pageable pageable);

    Optional<Product> findByIdAndOwner_Id(Long id, Long ownerId);

    // LOGIKA SEARCH & FILTER
    @Query("SELECT p FROM Product p " +
            "WHERE p.owner.id = :ownerId " +
            "AND (CAST(:keyword AS string) IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    Page<Product> searchByOwner(
            @Param("ownerId")     Long ownerId,
            @Param("keyword")    String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    long countByOwner(User owner);

    long countByOwnerAndActiveTrue(User owner);

    long countByOwnerAndActiveFalse(User owner);

    List<Product> findByOwnerAndStockLessThan(User owner, int stock);

    @Query("SELECT COALESCE(SUM(p.price * p.stock), 0) FROM Product p WHERE p.owner = :owner")
    long sumInventoryValue(@Param("owner") User owner);

    @Query("SELECT COALESCE(c.name, 'Tanpa Kategori'), COUNT(p) " +
            "FROM Product p LEFT JOIN p.category c " +
            "WHERE p.owner = :owner " +
            "GROUP BY c.name")
    List<Object[]> countByCategory(@Param("owner") User owner);
}