package gasi.gps.platform.infrastructure.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enables application-level caching.
 *
 * <p>
 * Cache provider settings are configured through Spring Boot's
 * {@code spring.cache.*} properties.
 * </p>
 *
 * @since 1.0.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Creates the cache configuration.
     */
    public CacheConfig() {
    }
}
