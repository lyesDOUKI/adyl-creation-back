package ld.application.config.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@TestConfiguration
public class TestClockConfiguration {

    public static final Instant FIXED_INSTANT = Instant.parse("2026-08-31T12:00:00Z");

    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(FIXED_INSTANT, ZoneId.of("UTC"));
    }
}
