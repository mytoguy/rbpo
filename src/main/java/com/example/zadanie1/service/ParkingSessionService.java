package com.example.zadanie1.service;

import com.example.zadanie1.entity.ParkingSession;
import com.example.zadanie1.entity.ParkingZone;
import com.example.zadanie1.entity.Vehicle;
import com.example.zadanie1.repository.ParkingPassRepository;
import com.example.zadanie1.repository.ParkingSessionRepository;
import com.example.zadanie1.repository.ParkingZoneRepository;
import com.example.zadanie1.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ParkingSessionService {

    private final ParkingSessionRepository sessionRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingZoneRepository zoneRepository;
    private final ParkingPassRepository passRepository;

    public ParkingSessionService(ParkingSessionRepository sessionRepository,
                                 VehicleRepository vehicleRepository,
                                 ParkingZoneRepository zoneRepository,
                                 ParkingPassRepository passRepository) {
        this.sessionRepository = sessionRepository;
        this.vehicleRepository = vehicleRepository;
        this.zoneRepository = zoneRepository;
        this.passRepository = passRepository;
    }

    public List<ParkingSession> getAll() {
        return sessionRepository.findAll();
    }

    public ParkingSession getById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ParkingSession not found with id: " + id));
    }

    @Transactional
    public ParkingSession startSession(Long vehicleId, Long zoneId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));
        ParkingZone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new RuntimeException("Zone not found: " + zoneId));

        // Бизнес-правило 1: Машина не должна иметь активных сессий
        boolean hasActiveSession = sessionRepository.findAll().stream()
                .anyMatch(s -> s.getVehicle().getId().equals(vehicleId) && s.getExitTime() == null);
        if (hasActiveSession) {
            throw new RuntimeException("Vehicle is already parked!");
        }

        // Бизнес-правило 2: В зоне должно быть свободное место
        long activeSessionsInZone = sessionRepository.findAll().stream()
                .filter(s -> s.getZone().getId().equals(zoneId) && s.getExitTime() == null)
                .count();
        if (activeSessionsInZone >= zone.getCapacity()) {
            throw new RuntimeException("Parking zone is full!");
        }

        ParkingSession session = new ParkingSession();
        session.setVehicle(vehicle);
        session.setZone(zone);
        session.setEntryTime(LocalDateTime.now());
        session.setExitTime(null);
        session.setTotalCost(null);

        return sessionRepository.save(session);
    }

    @Transactional
    public ParkingSession endSession(Long sessionId) {
        ParkingSession session = getById(sessionId);

        if (session.getExitTime() != null) {
            throw new RuntimeException("Session already ended");
        }

        LocalDateTime exitTime = LocalDateTime.now();
        session.setExitTime(exitTime);

        // Бизнес-правило: Проверка наличия активного абонемента
        boolean hasActivePass = passRepository.findAll().stream()
                .anyMatch(p -> p.getVehicle().getId().equals(session.getVehicle().getId())
                        && p.getZone().getId().equals(session.getZone().getId())
                        && "ACTIVE".equals(p.getStatus()));

        if (hasActivePass) {
            session.setTotalCost(BigDecimal.ZERO); // Бесплатно
        } else {
            long hours = ChronoUnit.HOURS.between(session.getEntryTime(), exitTime);
            if (hours == 0) hours = 1;
            BigDecimal cost = session.getZone().getHourlyRate().multiply(BigDecimal.valueOf(hours));
            session.setTotalCost(cost);
        }

        return sessionRepository.save(session);
    }

    public void delete(Long id) {
        sessionRepository.deleteById(id);
    }
}