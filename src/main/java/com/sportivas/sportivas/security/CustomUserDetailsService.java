package com.sportivas.sportivas.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sportivas.sportivas.model.User;
import com.sportivas.sportivas.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String insuranceNumber) throws UsernameNotFoundException {
        User user = userRepository.findByInsuranceNumber(insuranceNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with insurance number: " + insuranceNumber));

        return new org.springframework.security.core.userdetails.User(
                user.getInsuranceNumber(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}