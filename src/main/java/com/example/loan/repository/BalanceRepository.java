package com.example.loan.repository;

import com.example.loan.domain.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {

  Optional<Balance> findByApplicationId(Long applicationId);
}
