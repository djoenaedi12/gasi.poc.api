package gasi.gps.core.starter.infrastructure.crypto;

import java.math.BigDecimal;

import jakarta.persistence.Converter;

/**
 * JPA converter for BigDecimal fields that must be stored encrypted.
 *
 * @since 1.0.0
 */
@Converter
public class EncryptedBigDecimalConverter extends AbstractEncryptedAttributeConverter<BigDecimal> {

    /**
     * Creates a JPA encrypted big decimal converter.
     */
    public EncryptedBigDecimalConverter() {
    }

    @Override
    protected String serialize(BigDecimal attribute) {
        return attribute == null ? null : attribute.toPlainString();
    }

    @Override
    protected BigDecimal deserialize(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
