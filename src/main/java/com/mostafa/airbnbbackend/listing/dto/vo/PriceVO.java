package com.mostafa.airbnbbackend.listing.dto.vo;

import jakarta.validation.constraints.NotNull;

public record PriceVO(@NotNull(message = "Price value must be present") int value) {
}
