package com.sportivas.sportivas.repository;

import com.sportivas.sportivas.model.Sport;
import com.sportivas.sportivas.model.User;
import com.sportivas.sportivas.model.UserSport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSportRepository extends JpaRepository<UserSport, Long> {
    List<UserSport> findByUser(User user);
    Optional<UserSport> findByUserAndSport(User user, Sport sport);
}