package com.roomreservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.roomreservation.domain.entity.ReservationStatus;
import com.roomreservation.dto.ReservationRequestDTO;
import com.roomreservation.dto.ReservationResponseDTO;
import com.roomreservation.exception.GlobalExceptionHandler;
import com.roomreservation.exception.InvalidDateException;
import com.roomreservation.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID roomId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(reservationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        roomId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should create reservation and return 201")
    void shouldCreateReservationAndReturn201() throws Exception {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);

        ReservationRequestDTO request = ReservationRequestDTO.builder()
                .roomId(roomId)
                .reservedBy("John Doe")
                .startTime(startTime)
                .endTime(endTime)
                .status(ReservationStatus.PENDING)
                .build();

        ReservationResponseDTO response = ReservationResponseDTO.builder()
                .id(UUID.randomUUID())
                .roomId(roomId)
                .roomName("Conference A")
                .reservedBy("John Doe")
                .startTime(startTime)
                .endTime(endTime)
                .status(ReservationStatus.PENDING)
                .build();

        when(reservationService.create(any(ReservationRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservedBy").value("John Doe"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Should return 400 when DTO validation fails")
    void shouldReturn400WhenDtoValidationFails() throws Exception {
        String invalidPayload = """
                {
                  "roomId": null,
                  "reservedBy": "",
                  "startTime": null,
                  "endTime": null
                }
                """;

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.fieldMessages").isArray())
                .andExpect(jsonPath("$.fieldMessages[0].field").exists())
                .andExpect(jsonPath("$.fieldMessages[0].message").exists());
    }

    @Test
    @DisplayName("Should return 400 when business rule validation fails")
    void shouldReturn400WhenBusinessRuleValidationFails() throws Exception {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);

        ReservationRequestDTO request = ReservationRequestDTO.builder()
                .roomId(roomId)
                .reservedBy("John Doe")
                .startTime(startTime)
                .endTime(endTime)
                .build();

        when(reservationService.create(any(ReservationRequestDTO.class)))
                .thenThrow(new InvalidDateException("Start time must be before end time"));

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Business Rule Violation"))
                .andExpect(jsonPath("$.message").value("Start time must be before end time"));
    }
}
