package com.roomreservation.service;

import com.roomreservation.domain.entity.Room;
import com.roomreservation.dto.RoomRequestDTO;
import com.roomreservation.dto.RoomResponseDTO;
import com.roomreservation.exception.ReservationConflictException;
import com.roomreservation.exception.RoomNotFoundException;
import com.roomreservation.mapper.RoomMapper;
import com.roomreservation.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private RoomService roomService;

    private UUID roomId;
    private Room room;
    private RoomRequestDTO requestDTO;
    private RoomResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();
        room = Room.builder()
                .id(roomId)
                .name("Conference A")
                .capacity(10)
                .active(true)
                .build();

        requestDTO = RoomRequestDTO.builder()
                .name("Conference A")
                .capacity(10)
                .active(true)
                .build();

        responseDTO = RoomResponseDTO.builder()
                .id(roomId)
                .name("Conference A")
                .capacity(10)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should create room successfully")
    void shouldCreateRoomSuccessfully() {
        when(roomRepository.existsByName(requestDTO.getName())).thenReturn(false);
        when(roomMapper.toEntity(requestDTO)).thenReturn(room);
        when(roomRepository.save(room)).thenReturn(room);
        when(roomMapper.toResponseDTO(room)).thenReturn(responseDTO);

        RoomResponseDTO result = roomService.create(requestDTO);

        assertThat(result).isEqualTo(responseDTO);
        verify(roomRepository).save(room);
    }

    @Test
    @DisplayName("Should throw conflict when room name already exists")
    void shouldThrowConflictWhenRoomNameAlreadyExists() {
        when(roomRepository.existsByName(requestDTO.getName())).thenReturn(true);

        assertThatThrownBy(() -> roomService.create(requestDTO))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessageContaining("Room name already exists");

        verify(roomRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw not found when room does not exist")
    void shouldThrowNotFoundWhenRoomDoesNotExist() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.findById(roomId))
                .isInstanceOf(RoomNotFoundException.class)
                .hasMessageContaining("Room not found");
    }

    @Test
    @DisplayName("Should throw inactive room exception when room is inactive")
    void shouldThrowInactiveRoomExceptionWhenRoomIsInactive() {
        room.setActive(false);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> roomService.getActiveRoomOrThrow(roomId))
                .isInstanceOf(com.roomreservation.exception.InactiveRoomException.class)
                .hasMessageContaining("Room is inactive");
    }
}
