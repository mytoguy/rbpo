package com.example.zadanie1.service;

import com.example.zadanie1.entity.ParkingPass;
import com.example.zadanie1.entity.ParkingZone;
import com.example.zadanie1.entity.Vehicle;
import com.example.zadanie1.repository.ParkingPassRepository;
import com.example.zadanie1.repository.ParkingZoneRepository;
import com.example.zadanie1.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ParkingPassService {

    private final ParkingPassRepository passRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingZoneRepository zoneRepository;

    public ParkingPassService(ParkingPassRepository passRepository,
                              VehicleRepository vehicleRepository,
                              ParkingZoneRepository zoneRepository) {
        this.passRepository = passRepository;
        this.vehicleRepository = vehicleRepository;
        this.zoneRepository = zoneRepository;
    }

    public List<ParkingPass> getAll() {
        return passRepository.findAll();
    }

    public ParkingPass getById(Long id) {
        return passRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ParkingPass not found with id: " + id));
    }

    @Transactional
    public ParkingPass create(Long vehicleId, Long zoneId, LocalDate startDate, LocalDate endDate) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));
        ParkingZone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new RuntimeException("Zone not found: " + zoneId));

        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        // Бизнес-правило: Проверка на пересечение дат
        boolean hasOverlap = passRepository.findAll().stream()
                .filter(p -> p.getVehicle().getId().equals(vehicleId) && p.getZone().getId().equals(zoneId))
                .anyMatch(p -> !(endDate.isBefore(p.getStartDate()) || startDate.isAfter(p.getEndDate())));

        if (hasOverlap) {
            throw new RuntimeException("Overlapping parking pass already exists for this vehicle and zone");
        }

        ParkingPass pass = new ParkingPass();
        pass.setVehicle(vehicle);
        pass.setZone(zone);
        pass.setStartDate(startDate);
        pass.setEndDate(endDate);
        pass.setStatus(calculateStatus(startDate, endDate));

        return passRepository.save(pass);
    }

    public ParkingPass update(Long id, ParkingPass updated) {
        ParkingPass pass = getById(id);
        if (updated.getStartDate() != null) pass.setStartDate(updated.getStartDate());
        if (updated.getEndDate() != null) pass.setEndDate(updated.getEndDate());
        pass.setStatus(calculateStatus(pass.getStartDate(), pass.getEndDate()));
        return passRepository.save(pass);
    }

    public void delete(Long id) {
        passRepository.deleteById(id);
    }

    private String calculateStatus(LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();
        if (today.isBefore(start) || today.isAfter(end)) {
            return "EXPIRED";
        }
        return "ACTIVE";
    }
}