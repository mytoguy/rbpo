package com.example.zadanie1.service;

import com.example.zadanie1.entity.ParkingZone;
import com.example.zadanie1.repository.ParkingPassRepository;
import com.example.zadanie1.repository.ParkingSessionRepository;
import com.example.zadanie1.repository.ParkingZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ParkingZoneService {

    private final ParkingZoneRepository zoneRepository;
    private final ParkingSessionRepository sessionRepository;
    private final ParkingPassRepository passRepository;

    public ParkingZoneService(ParkingZoneRepository zoneRepository,
                              ParkingSessionRepository sessionRepository,
                              ParkingPassRepository passRepository) {
        this.zoneRepository = zoneRepository;
        this.sessionRepository = sessionRepository;
        this.passRepository = passRepository;
    }

    public List<ParkingZone> getAll() {
        return zoneRepository.findAll();
    }

    public ParkingZone getById(Long id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ParkingZone not found with id: " + id));
    }

    public ParkingZone create(ParkingZone zone) {
        return zoneRepository.save(zone);
    }

    public ParkingZone update(Long id, ParkingZone updated) {
        ParkingZone zone = getById(id);
        if (updated.getCapacity() != null) zone.setCapacity(updated.getCapacity());
        if (updated.getHourlyRate() != null) zone.setHourlyRate(updated.getHourlyRate());
        return zoneRepository.save(zone);
    }

    @Transactional
    public void delete(Long id) {
        getById(id); 

        boolean hasActiveSessions = sessionRepository.findAll().stream()
                .anyMatch(s -> s.getZone().getId().equals(id) && s.getExitTime() == null);
        if (hasActiveSessions) {
            throw new RuntimeException("Cannot delete zone with active parking sessions");
        }

        boolean hasActivePasses = passRepository.findAll().stream()
                .anyMatch(p -> p.getZone().getId().equals(id) && "ACTIVE".equals(p.getStatus()));
        if (hasActivePasses) {
            throw new RuntimeException("Cannot delete zone with active parking passes");
        }

        zoneRepository.deleteById(id);
    }
}