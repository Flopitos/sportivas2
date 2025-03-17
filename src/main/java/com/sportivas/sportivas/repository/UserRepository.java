package com.sportivas.sportivas.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sportivas.sportivas.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByInsuranceNumber(String insuranceNumber);
    boolean existsByInsuranceNumber(String insuranceNumber);
}