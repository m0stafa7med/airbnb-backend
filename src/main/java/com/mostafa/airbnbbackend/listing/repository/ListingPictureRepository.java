package com.mostafa.airbnbbackend.listing.repository;

import com.mostafa.airbnbbackend.listing.entity.ListingPicture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingPictureRepository extends JpaRepository<ListingPicture, Long> {
}
