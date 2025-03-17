package com.sportivas.sportivas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sportivas.sportivas.model.Injury;
import com.sportivas.sportivas.model.User;

public interface InjuryRepository extends JpaRepository<Injury, Long> {
    List<Injury> findByUserOrderByInjuryDateDesc(User user);
}