package com.mostafa.airbnbbackend.listing.service;


import com.mostafa.airbnbbackend.listing.dto.sub.PictureDTO;
import com.mostafa.airbnbbackend.listing.entity.Listing;
import com.mostafa.airbnbbackend.listing.entity.ListingPicture;
import com.mostafa.airbnbbackend.listing.mapper.ListingPictureMapper;
import com.mostafa.airbnbbackend.listing.repository.ListingPictureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class PictureService {

    private final ListingPictureRepository listingPictureRepository;
    private final ListingPictureMapper listingPictureMapper;

    public List<PictureDTO> saveAll(List<PictureDTO> pictures, Listing listing) {
        Set<ListingPicture> listingPictures = listingPictureMapper.pictureDTOsToListingPictures(pictures);

        boolean isFirst = true;

        for (ListingPicture listingPicture : listingPictures) {
            listingPicture.setCover(isFirst);
            listingPicture.setListing(listing);
            isFirst = false;
        }

        listingPictureRepository.saveAll(listingPictures);
        return listingPictureMapper.listingPictureToPictureDTO(listingPictures.stream().toList());
    }
}
