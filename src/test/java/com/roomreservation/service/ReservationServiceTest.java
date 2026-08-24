package com.roomreservation.service;

import com.roomreservation.domain.entity.Reservation;
import com.roomreservation.domain.entity.ReservationStatus;
import com.roomreservation.domain.entity.Room;
import com.roomreservation.dto.ReservationRequestDTO;
import com.roomreservation.dto.ReservationResponseDTO;
import com.roomreservation.exception.InactiveRoomException;
import com.roomreservation.exception.InvalidDateException;
import com.roomreservation.exception.ReservationConflictException;
import com.roomreservation.mapper.ReservationMapper;
import com.roomreservation.mapper.RoomMapper;
import com.roomreservation.repository.ReservationRepository;
import com.roomreservation.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private RoomMapper roomMapper;

    private RoomService roomService;
    private ReservationService reservationService;

    private UUID roomId;
    private Room room;
    private ReservationRequestDTO requestDTO;
    private Reservation reservation;
    private ReservationResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        roomService = new RoomService(roomRepository, roomMapper);
        reservationService = new ReservationService(reservationRepository, roomService, reservationMapper);

        roomId = UUID.randomUUID();

        room = Room.builder()
                .id(roomId)
                .name("Conference A")
                .capacity(10)
                .active(true)
                .build();

        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);

        requestDTO = ReservationRequestDTO.builder()
                .roomId(roomId)
                .reservedBy("John Doe")
                .startTime(startTime)
                .endTime(endTime)
                .status(ReservationStatus.CONFIRMED)
                .build();

        reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .room(room)
                .reservedBy("John Doe")
                .startTime(startTime)
                .endTime(endTime)
                .status(ReservationStatus.CONFIRMED)
                .build();

        responseDTO = ReservationResponseDTO.builder()
                .id(reservation.getId())
                .roomId(roomId)
                .roomName("Conference A")
                .reservedBy("John Doe")
                .startTime(startTime)
                .endTime(endTime)
                .status(ReservationStatus.CONFIRMED)
                .build();
    }

    @Test
    @DisplayName("Should create reservation successfully")
    void shouldCreateReservationSuccessfully() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(reservationRepository.existsOverlappingReservation(
                eq(roomId), any(), any(), eq(ReservationStatus.CONFIRMED)))
                .thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(reservationMapper.toResponseDTO(reservation)).thenReturn(responseDTO);

        ReservationResponseDTO result = reservationService.create(requestDTO);

        assertThat(result).isEqualTo(responseDTO);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should throw conflict when confirmed reservation overlaps")
    void shouldThrowConflictWhenConfirmedReservationOverlaps() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(reservationRepository.existsOverlappingReservation(
                eq(roomId), any(), any(), eq(ReservationStatus.CONFIRMED)))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationService.create(requestDTO))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessageContaining("confirmed reservation");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw invalid date when start time is not before end time")
    void shouldThrowInvalidDateWhenStartTimeIsNotBeforeEndTime() {
        requestDTO.setEndTime(requestDTO.getStartTime());

        assertThatThrownBy(() -> reservationService.create(requestDTO))
                .isInstanceOf(InvalidDateException.class)
                .hasMessageContaining("Start time must be before end time");

        verify(roomRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw invalid date when reservation is in the past")
    void shouldThrowInvalidDateWhenReservationIsInThePast() {
        requestDTO.setStartTime(LocalDateTime.now().minusHours(2));
        requestDTO.setEndTime(LocalDateTime.now().minusHours(1));

        assertThatThrownBy(() -> reservationService.create(requestDTO))
                .isInstanceOf(InvalidDateException.class)
                .hasMessageContaining("past");

        verify(roomRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw inactive room exception when room is inactive")
    void shouldThrowInactiveRoomExceptionWhenRoomIsInactive() {
        room.setActive(false);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> reservationService.create(requestDTO))
                .isInstanceOf(InactiveRoomException.class)
                .hasMessageContaining("Room is inactive");

        verify(reservationRepository, never()).save(any());
    }
}
