package com.mostafa.airbnbbackend.user.service;

import com.mostafa.airbnbbackend.config.SecurityUtils;
import com.mostafa.airbnbbackend.user.dto.ReadUserDTO;
import com.mostafa.airbnbbackend.user.entity.User;
import com.mostafa.airbnbbackend.user.mapper.UserMapper;
import com.mostafa.airbnbbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String UPDATED_AT_KEY = "updated_at";
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public ReadUserDTO getAuthenticatedUserFromSecurityContext() {
        Jwt jwt = (Jwt) ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getToken();
        User user = SecurityUtils.mapJwtToUser(jwt);
        return getByEmail(user.getEmail()).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Optional<ReadUserDTO> getByEmail(String email) {
        Optional<User> oneByEmail = userRepository.findOneByEmail(email);
        return oneByEmail.map(userMapper::readUserDTOToUser);
    }

    public void syncWithIdp(Jwt jwt, boolean forceResync) {
        Map<String, Object> claims = jwt.getClaims();
        User user = SecurityUtils.mapJwtToUser(jwt);
        Optional<User> existingUser = userRepository.findOneByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            if (claims.get(UPDATED_AT_KEY) != null) {
                Instant lastModifiedDate = existingUser.orElseThrow().getLastModifiedDate();
                Instant idpModifiedDate;
                if (claims.get(UPDATED_AT_KEY) instanceof Instant instant) {
                    idpModifiedDate = instant;
                } else {
                    idpModifiedDate = Instant.ofEpochSecond(((Number) claims.get(UPDATED_AT_KEY)).longValue());
                }
                if (idpModifiedDate.isAfter(lastModifiedDate) || forceResync) {
                    updateUser(user);
                }
            }
        } else {
            userRepository.saveAndFlush(user);
        }
    }

    private void updateUser(User user) {
        Optional<User> userToUpdateOpt = userRepository.findOneByEmail(user.getEmail());
        if (userToUpdateOpt.isPresent()) {
            User userToUpdate = userToUpdateOpt.get();
            userToUpdate.setEmail(user.getEmail());
            userToUpdate.setFirstName(user.getFirstName());
            userToUpdate.setLastName(user.getLastName());
            userToUpdate.setAuthorities(user.getAuthorities());
            userToUpdate.setImageUrl(user.getImageUrl());
            userRepository.saveAndFlush(userToUpdate);
        }
    }

    public Optional<ReadUserDTO> getByPublicId(UUID publicId) {
        Optional<User> oneByPublicId = userRepository.findOneByPublicId(publicId);
        return oneByPublicId.map(userMapper::readUserDTOToUser);
    }

}
