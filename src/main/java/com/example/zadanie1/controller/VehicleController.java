package com.example.zadanie1.controller;

import com.example.zadanie1.entity.Vehicle;
import com.example.zadanie1.service.VehicleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public ResponseEntity<List<Vehicle>> getAll() {
        return ResponseEntity.ok(vehicleService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Vehicle> create(@RequestBody Vehicle vehicle, @RequestParam Long ownerId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(vehicle, ownerId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Vehicle> update(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        return ResponseEntity.ok(vehicleService.update(id, vehicle));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/transfer")
    public ResponseEntity<Vehicle> transferVehicle(@PathVariable Long id, @RequestParam Long newOwnerId) {
        return ResponseEntity.ok(vehicleService.transferVehicle(id, newOwnerId));
    }
}