package com.example.zadanie1.service;

import com.example.zadanie1.entity.ParkingSession;
import com.example.zadanie1.entity.ParkingZone;
import com.example.zadanie1.entity.Vehicle;
import com.example.zadanie1.repository.ParkingSessionRepository;
import com.example.zadanie1.repository.ParkingZoneRepository;
import com.example.zadanie1.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ParkingSessionService {

    private final ParkingSessionRepository sessionRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingZoneRepository zoneRepository;

    public ParkingSessionService(ParkingSessionRepository sessionRepository,
                                 VehicleRepository vehicleRepository,
                                 ParkingZoneRepository zoneRepository) {
        this.sessionRepository = sessionRepository;
        this.vehicleRepository = vehicleRepository;
        this.zoneRepository = zoneRepository;
    }

    public List<ParkingSession> getAll() {
        return sessionRepository.findAll();
    }

    public ParkingSession getById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ParkingSession not found with id: " + id));
    }

    public ParkingSession startSession(Long vehicleId, Long zoneId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));
        ParkingZone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new RuntimeException("Zone not found: " + zoneId));

        ParkingSession session = new ParkingSession();
        session.setVehicle(vehicle);
        session.setZone(zone);
        session.setEntryTime(LocalDateTime.now()); // Время задает СЕРВЕР
        session.setExitTime(null);
        session.setTotalCost(null);

        return sessionRepository.save(session);
    }

    
    public ParkingSession endSession(Long sessionId) {
        ParkingSession session = getById(sessionId);

        if (session.getExitTime() != null) {
            throw new RuntimeException("Session already ended");
        }

        LocalDateTime exitTime = LocalDateTime.now(); // Время задает СЕРВЕР
        session.setExitTime(exitTime);

        long hours = ChronoUnit.HOURS.between(session.getEntryTime(), exitTime);
        if (hours == 0) hours = 1; // минимум 1 час

        BigDecimal cost = session.getZone().getHourlyRate()
                .multiply(BigDecimal.valueOf(hours));
        session.setTotalCost(cost);

        return sessionRepository.save(session);
    }

    public ParkingSession update(Long id, ParkingSession updated) {
        ParkingSession session = getById(id);
        // Разрешаем менять только привязку vehicle/zone (по желанию)
        return sessionRepository.save(session);
    }

    public void delete(Long id) {
        sessionRepository.deleteById(id);
    }
}