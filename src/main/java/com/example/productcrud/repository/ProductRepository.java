package com.example.productcrud.repository;

import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByOwner(Long ownerId, Pageable pageable);

    Optional<Product> findByIdAndOwnerId(Long id, Long ownerId);
} //usernya ganti ke id