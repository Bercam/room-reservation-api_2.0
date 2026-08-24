package com.roomreservation.controller;

import com.roomreservation.dto.ReservationRequestDTO;
import com.roomreservation.dto.ReservationResponseDTO;
import com.roomreservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Reservation management endpoints")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @Operation(summary = "List all reservations")
    public ResponseEntity<List<ReservationResponseDTO>> findAll() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by id")
    public ResponseEntity<ReservationResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new reservation")
    public ResponseEntity<ReservationResponseDTO> create(@Valid @RequestBody ReservationRequestDTO request) {
        ReservationResponseDTO response = reservationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing reservation")
    public ResponseEntity<ReservationResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody ReservationRequestDTO request
    ) {
        return ResponseEntity.ok(reservationService.update(id, request));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a reservation")
    public ResponseEntity<ReservationResponseDTO> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.confirm(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a reservation")
    public ResponseEntity<ReservationResponseDTO> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.cancel(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a reservation")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
