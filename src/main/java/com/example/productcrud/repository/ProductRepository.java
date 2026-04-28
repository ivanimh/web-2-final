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


    long countByOwner(User owner);

    long countByOwnerAndActiveTrue(User owner);

    long countByOwnerAndActiveFalse(User owner);

    @Query("SELECT COALESCE(SUM(p.price * p.stock), 0) FROM Product p WHERE p.owner = :owner")
    long sumInventoryValue(@Param("owner") User owner);

    List<Product> findByOwnerAndStockLessThan(User owner, int stockThreshold);

    @Query("SELECT p.category.name, COUNT(p) FROM Product p WHERE p.owner = :owner AND p.category IS NOT NULL GROUP BY p.category.name")
    List<Object[]> countByCategory(@Param("owner") User owner);
}