package app.web;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@ActiveProfiles("test")
public class SecurityTestConfig {
    public static final String TEST_API_KEY = "api-key";
}