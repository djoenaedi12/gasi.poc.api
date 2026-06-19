package gasi.gps.core.starter.infrastructure.crypto;

import java.time.LocalDate;

import jakarta.persistence.Converter;

/**
 * JPA converter for LocalDate fields that must be stored encrypted.
 *
 * @since 1.0.0
 */
@Converter
public class EncryptedLocalDateConverter extends AbstractEncryptedAttributeConverter<LocalDate> {

    /**
     * Creates a JPA encrypted local date converter.
     */
    public EncryptedLocalDateConverter() {
    }

    @Override
    protected String serialize(LocalDate attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    protected LocalDate deserialize(String value) {
        return value == null ? null : LocalDate.parse(value);
    }
}
