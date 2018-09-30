package com.csye6225.repository;

import com.csye6225.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionJpaRepository extends JpaRepository<Transaction, String> {
}
