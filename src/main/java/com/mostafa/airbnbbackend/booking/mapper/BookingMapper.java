package com.mostafa.airbnbbackend.booking.mapper;


import com.mostafa.airbnbbackend.booking.dto.BookedDateDTO;
import com.mostafa.airbnbbackend.booking.dto.NewBookingDTO;
import com.mostafa.airbnbbackend.booking.entity.Booking;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    Booking newBookingToBooking(NewBookingDTO newBookingDTO);

    BookedDateDTO bookingToCheckAvailability(Booking booking);
}
