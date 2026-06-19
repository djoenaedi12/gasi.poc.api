package gasi.gps.core.starter.infrastructure.crypto;

import jakarta.persistence.AttributeConverter;

/**
 * Base JPA converter for fields that must be serialized to String before
 * encrypted persistence.
 *
 * @param <T> entity attribute type
 * @since 1.0.0
 */
abstract class AbstractEncryptedAttributeConverter<T> implements AttributeConverter<T, String> {

    private static volatile FieldEncryptor fieldEncryptor;

    static void configure(FieldEncryptor encryptor) {
        fieldEncryptor = encryptor;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        return encryptor().encrypt(serialize(attribute));
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        return deserialize(encryptor().decrypt(dbData));
    }

    protected abstract String serialize(T attribute);

    protected abstract T deserialize(String value);

    private static FieldEncryptor encryptor() {
        if (fieldEncryptor == null) {
            throw new FieldEncryptionException("Field encryption is not initialized");
        }
        return fieldEncryptor;
    }
}
