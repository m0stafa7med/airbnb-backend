package com.mostafa.airbnbbackend.listing.dto.vo;

import jakarta.validation.constraints.NotNull;

public record BathsVO(@NotNull(message = "Bath value must be present") int value) {
}
