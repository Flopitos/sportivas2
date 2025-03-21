package com.sportivas.sportivas.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sportivas.sportivas.model.Sport;

public interface SportRepository extends JpaRepository<Sport, Long> {
    Optional<Sport> findByName(String name);
}