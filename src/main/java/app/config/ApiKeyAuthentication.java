package app.config;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
@RequiredArgsConstructor
public class ApiKeyAuthentication implements Authentication {
    private final String apiKey;
    private boolean authenticated = true;

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nullable Object getCredentials() {
        return apiKey;
    }

    @Override
    public @Nullable Object getDetails() {
        return null;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return apiKey;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return "API-Key";
    }
}
