package com.mostafa.airbnbbackend.config;

import com.mostafa.airbnbbackend.user.entity.Authority;
import com.mostafa.airbnbbackend.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SecurityUtils {

    public static final String ROLE_TENANT = "ROLE_TENANT";
    public static final String ROLE_LANDLORD = "ROLE_LANDLORD";

    public static final String CLAIMS_NAMESPACE = "https://www.darwesh.com/roles";

    public static User mapJwtToUser(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();
        User user = new User();

        if (claims.get("given_name") != null) {
            user.setFirstName((String) claims.get("given_name"));
        } else if (claims.get("nickname") != null) {
            user.setFirstName((String) claims.get("nickname"));
        }

        if (claims.get("family_name") != null) {
            user.setLastName((String) claims.get("family_name"));
        }

        if (claims.get("email") != null) {
            user.setEmail((String) claims.get("email"));
        } else {
            user.setEmail(jwt.getSubject());
        }

        if (claims.get("picture") != null) {
            user.setImageUrl((String) claims.get("picture"));
        }

        if (claims.get(CLAIMS_NAMESPACE) != null) {
            @SuppressWarnings("unchecked")
            List<String> authoritiesRaw = (List<String>) claims.get(CLAIMS_NAMESPACE);
            Set<Authority> authorities = authoritiesRaw.stream()
                    .map(authority -> {
                        Authority auth = new Authority();
                        auth.setName(authority);
                        return auth;
                    }).collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }
        return user;
    }

    public static List<SimpleGrantedAuthority> extractAuthorityFromClaims(Map<String, Object> claims) {
        return mapRolesToGrantedAuthorities(getRolesFromClaims(claims));
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> getRolesFromClaims(Map<String, Object> claims) {
        return (List<String>) claims.getOrDefault(CLAIMS_NAMESPACE, List.of());
    }

    private static List<SimpleGrantedAuthority> mapRolesToGrantedAuthorities(Collection<String> roles) {
        return roles.stream().filter(role -> role.startsWith("ROLE_")).map(SimpleGrantedAuthority::new).toList();
    }

    public static boolean hasCurrentUserAnyOfAuthorities(String... authorities) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && getAuthorities(authentication)
                .anyMatch(authority -> Arrays.asList(authorities).contains(authority)));
    }

    private static Stream<String> getAuthorities(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication
                instanceof JwtAuthenticationToken jwtAuthenticationToken
                ? extractAuthorityFromClaims(jwtAuthenticationToken.getToken().getClaims())
                : authentication.getAuthorities();
        return authorities.stream().map(GrantedAuthority::getAuthority);
    }
}
