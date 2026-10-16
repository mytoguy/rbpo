package com.example.zadanie1.controller;

import com.example.zadanie1.entity.ParkingSession;
import com.example.zadanie1.service.ParkingSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-sessions")
public class ParkingSessionController {

    private final ParkingSessionService sessionService;

    public ParkingSessionController(ParkingSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public ResponseEntity<List<ParkingSession>> getAll() {
        return ResponseEntity.ok(sessionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingSession> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getById(id));
    }

    // Начать парковку
    @PostMapping
    public ResponseEntity<ParkingSession> start(@RequestParam Long vehicleId,
                                                @RequestParam Long zoneId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionService.startSession(vehicleId, zoneId));
    }

    // Завершить парковку (рассчитывается сумма)
    @PatchMapping("/{id}")
    public ResponseEntity<ParkingSession> end(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.endSession(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sessionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}