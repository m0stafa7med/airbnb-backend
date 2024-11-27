package com.mostafa.airbnbbackend.listing.dto;


import com.mostafa.airbnbbackend.listing.dto.vo.PriceVO;

import java.util.UUID;

public record ListingCreateBookingDTO(
        UUID listingPublicId, PriceVO price) {
}
