package uk.gov.saas.dsa.web.helper;

import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextHelper {

    public static SecurityContext securityContext() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (Objects.isNull(securityContext) || Objects.isNull(securityContext.getAuthentication().getPrincipal())
                || securityContext.getAuthentication().getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return securityContext;
    }

    public static String getLoggedInUser() {
        String loggedInUser = "DEFAULT_USER";
        SecurityContext securityContext = securityContext();
        if (Objects.nonNull(securityContext)) {
            loggedInUser = (String) securityContext.getAuthentication().getAuthorities().stream()
                    .map(ga -> ga.getAuthority())
                    .filter(g -> g.contains("USER_ID"))
                    .findFirst()
                    .get()
                    .split(",")[1];
            return loggedInUser;
        }
        return loggedInUser;
    }

    public static String getLoggedInUserRole() {
        String role = "DEFAULT_USER";
        SecurityContext securityContext = securityContext();
        if (Objects.nonNull(securityContext)) {
            role = (String) securityContext.getAuthentication().getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(g -> g.contains("ROLE"))
                    .findFirst()
                    .orElse("ROLE, ROLE_NOT_SET")
                    .split(",")[1];
            return role;
        }
        return role;
    }
}
