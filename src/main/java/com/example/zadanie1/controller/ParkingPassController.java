package com.example.zadanie1.controller;

import com.example.zadanie1.entity.ParkingPass;
import com.example.zadanie1.service.ParkingPassService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/parking-passes")
public class ParkingPassController {

    private final ParkingPassService passService;

    public ParkingPassController(ParkingPassService passService) {
        this.passService = passService;
    }

    @GetMapping
    public ResponseEntity<List<ParkingPass>> getAll() {
        return ResponseEntity.ok(passService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingPass> getById(@PathVariable Long id) {
        return ResponseEntity.ok(passService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ParkingPass> create(
            @RequestParam Long vehicleId,
            @RequestParam Long zoneId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(passService.create(vehicleId, zoneId, startDate, endDate));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ParkingPass> update(@PathVariable Long id, @RequestBody ParkingPass pass) {
        return ResponseEntity.ok(passService.update(id, pass));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        passService.delete(id);
        return ResponseEntity.noContent().build();
    }
}