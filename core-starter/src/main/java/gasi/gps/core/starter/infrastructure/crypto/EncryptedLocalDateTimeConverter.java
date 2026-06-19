package gasi.gps.core.starter.infrastructure.crypto;

import java.time.LocalDateTime;

import jakarta.persistence.Converter;

/**
 * JPA converter for LocalDateTime fields that must be stored encrypted.
 *
 * @since 1.0.0
 */
@Converter
public class EncryptedLocalDateTimeConverter extends AbstractEncryptedAttributeConverter<LocalDateTime> {

    /**
     * Creates a JPA encrypted local date-time converter.
     */
    public EncryptedLocalDateTimeConverter() {
    }

    @Override
    protected String serialize(LocalDateTime attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    protected LocalDateTime deserialize(String value) {
        return value == null ? null : LocalDateTime.parse(value);
    }
}
