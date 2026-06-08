package gasi.gps.core.starter.infrastructure.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for field-level encryption used by JPA converters.
 *
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "app.field-encryption")
public class FieldEncryptionProperties {

    /** Whether field-level encryption is enabled. */
    private boolean enabled;

    /** Base64-encoded AES key. */
    private String key;

    /**
     * Creates empty field-encryption properties.
     */
    public FieldEncryptionProperties() {
    }

    /**
     * Indicates whether field-level encryption is enabled.
     *
     * @return {@code true} when encryption is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether field-level encryption is enabled.
     *
     * @param enabled {@code true} to enable encryption
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the Base64-encoded AES key.
     *
     * @return Base64-encoded AES key, or {@code null} when not configured
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the Base64-encoded AES key.
     *
     * @param key Base64-encoded AES key
     */
    public void setKey(String key) {
        this.key = key;
    }
}
