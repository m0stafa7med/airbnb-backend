package com.mostafa.airbnbbackend.listing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mostafa.airbnbbackend.config.SecurityUtils;
import com.mostafa.airbnbbackend.listing.dto.CreatedListingDTO;
import com.mostafa.airbnbbackend.listing.dto.DisplayCardListingDTO;
import com.mostafa.airbnbbackend.listing.dto.SaveListingDTO;
import com.mostafa.airbnbbackend.listing.dto.sub.PictureDTO;
import com.mostafa.airbnbbackend.listing.service.LandlordService;
import com.mostafa.airbnbbackend.shared.dto.State;
import com.mostafa.airbnbbackend.shared.dto.StatusNotification;
import com.mostafa.airbnbbackend.user.dto.ReadUserDTO;
import com.mostafa.airbnbbackend.user.exception.UserException;
import com.mostafa.airbnbbackend.user.service.UserService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/landlord-listing")
public class LandlordController {

    private final LandlordService landlordService;
    private final Validator validator;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(MultipartHttpServletRequest request,
                                    @RequestPart("dto") String saveListingDTOString) throws IOException {

        SaveListingDTO saveListingDTO = parseAndAttachPictures(request, saveListingDTOString);

        Set<ConstraintViolation<SaveListingDTO>> violations = validator.validate(saveListingDTO);
        if (!violations.isEmpty()) {
            return buildValidationErrorResponse(violations);
        }

        CreatedListingDTO createdListing = landlordService.create(saveListingDTO);
        return ResponseEntity.ok(createdListing);
    }

    @GetMapping("/get-all")
    @PreAuthorize("hasAnyRole('" + SecurityUtils.ROLE_LANDLORD + "')")
    public ResponseEntity<List<DisplayCardListingDTO>> getAll() {
        ReadUserDTO connectedUser = getConnectedUser();
        return ResponseEntity.ok(landlordService.getAllProperties(connectedUser));
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('" + SecurityUtils.ROLE_LANDLORD + "')")
    public ResponseEntity<UUID> delete(@RequestParam UUID publicId) {
        ReadUserDTO connectedUser = getConnectedUser();
        State<UUID, String> result = landlordService.delete(publicId, connectedUser);

        if (result.getStatus() == StatusNotification.OK) {
            return ResponseEntity.ok(result.getValue());
        }

        if (result.getStatus() == StatusNotification.UNAUTHORIZED) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    private SaveListingDTO parseAndAttachPictures(MultipartHttpServletRequest request,
            String saveListingDTOString) throws IOException {
        SaveListingDTO dto = objectMapper.readValue(saveListingDTOString, SaveListingDTO.class);
        dto.setPictures(extractPictures(request));
        return dto;
    }

    private List<PictureDTO> extractPictures(MultipartHttpServletRequest request) {
        return request.getFileMap()
                .values()
                .stream()
                .map(this::mapToPictureDTO)
                .toList();
    }

    private PictureDTO mapToPictureDTO(MultipartFile file) {
        try {
            return new PictureDTO(file.getBytes(), file.getContentType(), false);
        } catch (IOException e) {
            throw new UserException(
                    "Cannot parse multipart file: " + file.getOriginalFilename()
            );
        }
    }

    private ResponseEntity<ProblemDetail> buildValidationErrorResponse(
            Set<ConstraintViolation<SaveListingDTO>> violations
    ) {
        String message = violations.stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining(", "));

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);

        return ResponseEntity.badRequest().body(problemDetail);
    }

    private ReadUserDTO getConnectedUser() {
        return userService.getAuthenticatedUserFromSecurityContext();
    }
}
