package gasi.gps.core.starter.infrastructure.crypto;

import jakarta.persistence.Converter;

/**
 * JPA converter for String fields that must be stored encrypted.
 *
 * @since 1.0.0
 */
@Converter
public class EncryptedStringConverter extends AbstractEncryptedAttributeConverter<String> {

    /**
     * Creates a JPA encrypted string converter.
     */
    public EncryptedStringConverter() {
    }

    @Override
    protected String serialize(String attribute) {
        return attribute;
    }

    @Override
    protected String deserialize(String value) {
        return value;
    }
}
