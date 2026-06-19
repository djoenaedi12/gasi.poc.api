package gasi.gps.core.starter.infrastructure.crypto;

import jakarta.persistence.Converter;

/**
 * JPA converter for Long fields that must be stored encrypted.
 *
 * @since 1.0.0
 */
@Converter
public class EncryptedLongConverter extends AbstractEncryptedAttributeConverter<Long> {

    /**
     * Creates a JPA encrypted long converter.
     */
    public EncryptedLongConverter() {
    }

    @Override
    protected String serialize(Long attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    protected Long deserialize(String value) {
        return value == null ? null : Long.valueOf(value);
    }
}
