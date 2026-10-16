package com.example.zadanie1.repository;

import com.example.zadanie1.entity.ParkingPass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingPassRepository extends JpaRepository<ParkingPass, Long> {
}