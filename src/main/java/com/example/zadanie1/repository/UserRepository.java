package com.example.zadanie1.repository;

import com.example.zadanie1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}