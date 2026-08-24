package com.roomreservation.controller;

import com.roomreservation.dto.RoomRequestDTO;
import com.roomreservation.dto.RoomResponseDTO;
import com.roomreservation.service.RoomService;
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
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms", description = "Room management endpoints")
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    @Operation(summary = "List all rooms")
    public ResponseEntity<List<RoomResponseDTO>> findAll() {
        return ResponseEntity.ok(roomService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get room by id")
    public ResponseEntity<RoomResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(roomService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new room")
    public ResponseEntity<RoomResponseDTO> create(@Valid @RequestBody RoomRequestDTO request) {
        RoomResponseDTO response = roomService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing room")
    public ResponseEntity<RoomResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody RoomRequestDTO request
    ) {
        return ResponseEntity.ok(roomService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a room")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
