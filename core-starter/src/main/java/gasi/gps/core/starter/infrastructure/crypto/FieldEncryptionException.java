package gasi.gps.core.starter.infrastructure.crypto;

/**
 * Runtime failure while encrypting or decrypting persisted field values.
 *
 * @since 1.0.0
 */
public class FieldEncryptionException extends RuntimeException {

    /**
     * Creates an encryption exception with a detail message.
     *
     * @param message detail message
     */
    public FieldEncryptionException(String message) {
        super(message);
    }

    /**
     * Creates an encryption exception with a detail message and root cause.
     *
     * @param message detail message
     * @param cause   root cause
     */
    public FieldEncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
