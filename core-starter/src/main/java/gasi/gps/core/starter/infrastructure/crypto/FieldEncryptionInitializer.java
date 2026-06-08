package gasi.gps.core.starter.infrastructure.crypto;

import org.springframework.stereotype.Component;

/**
 * Connects the Spring-managed encryptor to JPA converter instances.
 *
 * @since 1.0.0
 */
@Component
public class FieldEncryptionInitializer {

    /**
     * Registers the Spring-managed encryptor for JPA converter instances.
     *
     * @param fieldEncryptor encryptor to use from JPA converters
     */
    public FieldEncryptionInitializer(FieldEncryptor fieldEncryptor) {
        EncryptedStringConverter.configure(fieldEncryptor);
    }
}
