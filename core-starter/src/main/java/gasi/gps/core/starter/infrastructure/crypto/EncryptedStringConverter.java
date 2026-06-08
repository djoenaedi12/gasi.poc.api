package gasi.gps.core.starter.infrastructure.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for String fields that must be stored encrypted.
 *
 * @since 1.0.0
 */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static volatile FieldEncryptor fieldEncryptor;

    /**
     * Creates a JPA encrypted string converter.
     */
    public EncryptedStringConverter() {
    }

    static void configure(FieldEncryptor encryptor) {
        fieldEncryptor = encryptor;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return encryptor().encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return encryptor().decrypt(dbData);
    }

    private static FieldEncryptor encryptor() {
        if (fieldEncryptor == null) {
            throw new FieldEncryptionException("Field encryption is not initialized");
        }
        return fieldEncryptor;
    }
}
