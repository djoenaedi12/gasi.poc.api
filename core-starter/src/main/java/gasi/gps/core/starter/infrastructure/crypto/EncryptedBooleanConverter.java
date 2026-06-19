package gasi.gps.core.starter.infrastructure.crypto;

import jakarta.persistence.Converter;

/**
 * JPA converter for Boolean fields that must be stored encrypted.
 *
 * @since 1.0.0
 */
@Converter
public class EncryptedBooleanConverter extends AbstractEncryptedAttributeConverter<Boolean> {

    /**
     * Creates a JPA encrypted boolean converter.
     */
    public EncryptedBooleanConverter() {
    }

    @Override
    protected String serialize(Boolean attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    protected Boolean deserialize(String value) {
        if (value == null) {
            return null;
        }
        if ("true".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }
        throw new FieldEncryptionException("Invalid encrypted boolean value");
    }
}
