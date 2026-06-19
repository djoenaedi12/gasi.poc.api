package gasi.gps.core.starter.infrastructure.crypto;

import jakarta.persistence.Converter;

/**
 * JPA converter for Integer fields that must be stored encrypted.
 *
 * @since 1.0.0
 */
@Converter
public class EncryptedIntegerConverter extends AbstractEncryptedAttributeConverter<Integer> {

    /**
     * Creates a JPA encrypted integer converter.
     */
    public EncryptedIntegerConverter() {
    }

    @Override
    protected String serialize(Integer attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    protected Integer deserialize(String value) {
        return value == null ? null : Integer.valueOf(value);
    }
}
