package com.example.zadanie1.repository;

import com.example.zadanie1.entity.ParkingSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {
}