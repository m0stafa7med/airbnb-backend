package com.mostafa.airbnbbackend.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record BookedDateDTO(
        @NotNull OffsetDateTime startDate,
        @NotNull OffsetDateTime endDate
) {
}
