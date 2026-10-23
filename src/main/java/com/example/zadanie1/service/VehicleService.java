package com.example.zadanie1.service;

import com.example.zadanie1.entity.User;
import com.example.zadanie1.entity.Vehicle;
import com.example.zadanie1.repository.UserRepository;
import com.example.zadanie1.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public VehicleService(VehicleRepository vehicleRepository, UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    public List<Vehicle> getAll() {
        return vehicleRepository.findAll();
    }

    public Vehicle getById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));
    }

    public Vehicle create(Vehicle vehicle, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found with id: " + ownerId));
        vehicle.setOwner(owner);
        return vehicleRepository.save(vehicle);
    }

    public Vehicle update(Long id, Vehicle updated) {
        Vehicle vehicle = getById(id);
        if (updated.getLicensePlate() != null) {
            vehicle.setLicensePlate(updated.getLicensePlate());
        }
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle transferVehicle(Long vehicleId, Long newOwnerId) {
        Vehicle vehicle = getById(vehicleId);
        User newOwner = userRepository.findById(newOwnerId)
                .orElseThrow(() -> new RuntimeException("New owner not found: " + newOwnerId));

        boolean isParked = vehicle.getSessions().stream()
                .anyMatch(s -> s.getExitTime() == null);
        if (isParked) {
            throw new RuntimeException("Cannot transfer vehicle while it is parked");
        }

        vehicle.setOwner(newOwner);
        return vehicleRepository.save(vehicle);
    }

    public void delete(Long id) {
        vehicleRepository.deleteById(id);
    }
}