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
import com.mostafa.airbnbbackend.user.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/landlord-listing")
public class LandlordController {

    private static final String DTO_PART_NAME = "dto";

    private final LandlordService landlordService;
    private final Validator validator;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(HttpServletRequest request) throws IOException, ServletException {
        Collection<Part> parts = request.getParts();
        if (parts.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Empty multipart request"));
        }

        String dtoJson = null;
        List<PictureDTO> pictures = new ArrayList<>();

        for (Part part : parts) {
            if (DTO_PART_NAME.equals(part.getName())) {
                dtoJson = readPartAsString(part);
            } else {
                pictures.add(readPartAsPicture(part));
            }
        }

        if (dtoJson == null) {
            return ResponseEntity.badRequest()
                    .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Missing 'dto' part"));
        }

        SaveListingDTO saveListingDTO = objectMapper.readValue(dtoJson, SaveListingDTO.class);
        saveListingDTO.setPictures(pictures);

        Set<ConstraintViolation<SaveListingDTO>> violations = validator.validate(saveListingDTO);
        if (!violations.isEmpty()) {
            return buildValidationErrorResponse(violations);
        }

        CreatedListingDTO createdListing = landlordService.create(saveListingDTO);
        return ResponseEntity.ok(createdListing);
    }

    @GetMapping("/get-all")
    @PreAuthorize("hasAuthority('" + SecurityUtils.ROLE_LANDLORD + "')")
    public ResponseEntity<List<DisplayCardListingDTO>> getAll() {
        ReadUserDTO connectedUser = getConnectedUser();
        return ResponseEntity.ok(landlordService.getAllProperties(connectedUser));
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('" + SecurityUtils.ROLE_LANDLORD + "')")
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

    private String readPartAsString(Part part) throws IOException {
        try (InputStream in = part.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private PictureDTO readPartAsPicture(Part part) throws IOException {
        try (InputStream in = part.getInputStream()) {
            return new PictureDTO(in.readAllBytes(), part.getContentType(), false);
        }
    }

    private ResponseEntity<ProblemDetail> buildValidationErrorResponse(
            Set<ConstraintViolation<SaveListingDTO>> violations) {
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
