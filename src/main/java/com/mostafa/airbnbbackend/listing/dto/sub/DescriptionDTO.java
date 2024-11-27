package com.mostafa.airbnbbackend.listing.dto.sub;


import com.mostafa.airbnbbackend.listing.dto.vo.DescriptionVO;
import com.mostafa.airbnbbackend.listing.dto.vo.TitleVO;
import jakarta.validation.constraints.NotNull;

public record DescriptionDTO(
        @NotNull TitleVO title,
        @NotNull DescriptionVO description
        ) {
}
