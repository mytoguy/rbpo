package com.example.zadanie1.service;

import com.example.zadanie1.entity.ParkingZone;
import com.example.zadanie1.repository.ParkingZoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParkingZoneService {

    private final ParkingZoneRepository zoneRepository;

    public ParkingZoneService(ParkingZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
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

    public void delete(Long id) {
        zoneRepository.deleteById(id);
    }
}