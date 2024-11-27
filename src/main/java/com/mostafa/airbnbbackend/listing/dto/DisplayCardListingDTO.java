package com.mostafa.airbnbbackend.listing.dto;



import com.mostafa.airbnbbackend.listing.dto.sub.PictureDTO;
import com.mostafa.airbnbbackend.listing.dto.vo.PriceVO;
import com.mostafa.airbnbbackend.listing.enums.BookingCategory;

import java.util.UUID;

public record DisplayCardListingDTO(PriceVO price,
                                    String location,
                                    PictureDTO cover,
                                    BookingCategory bookingCategory,
                                    UUID publicId) {
}
