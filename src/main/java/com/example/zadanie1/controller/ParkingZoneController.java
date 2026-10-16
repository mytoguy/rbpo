package com.example.zadanie1.controller;

import com.example.zadanie1.entity.ParkingZone;
import com.example.zadanie1.service.ParkingZoneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-zones")
public class ParkingZoneController {

    private final ParkingZoneService zoneService;

    public ParkingZoneController(ParkingZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @GetMapping
    public ResponseEntity<List<ParkingZone>> getAll() {
        return ResponseEntity.ok(zoneService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingZone> getById(@PathVariable Long id) {
        return ResponseEntity.ok(zoneService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ParkingZone> create(@RequestBody ParkingZone zone) {
        return ResponseEntity.status(HttpStatus.CREATED).body(zoneService.create(zone));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ParkingZone> update(@PathVariable Long id, @RequestBody ParkingZone zone) {
        return ResponseEntity.ok(zoneService.update(id, zone));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        zoneService.delete(id);
        return ResponseEntity.noContent().build();
    }
}