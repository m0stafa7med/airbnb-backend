package com.mostafa.airbnbbackend.listing.dto.sub;


import com.mostafa.airbnbbackend.listing.dto.vo.BathsVO;
import com.mostafa.airbnbbackend.listing.dto.vo.BedroomsVO;
import com.mostafa.airbnbbackend.listing.dto.vo.BedsVO;
import com.mostafa.airbnbbackend.listing.dto.vo.GuestsVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ListingInfoDTO(
        @NotNull @Valid GuestsVO guests,
        @NotNull @Valid BedroomsVO bedrooms,
        @NotNull @Valid BedsVO beds,
        @NotNull @Valid BathsVO baths) {
}
