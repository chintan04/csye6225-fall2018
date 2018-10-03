package com.csye6225.repository;

import com.csye6225.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<Transaction, UUID> {
    void delete(UUID id);
    Transaction findOne(UUID id);
}
