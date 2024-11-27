package com.mostafa.airbnbbackend.listing.dto;


import com.mostafa.airbnbbackend.booking.dto.BookedDateDTO;
import com.mostafa.airbnbbackend.listing.dto.sub.ListingInfoDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record SearchDTO(@Valid BookedDateDTO dates,
                        @Valid ListingInfoDTO infos,
                        @NotEmpty String location) {
}
