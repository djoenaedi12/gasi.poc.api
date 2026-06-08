package gasi.gps.core.starter.infrastructure.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

/**
 * Encrypts and decrypts String values for field-level persistence.
 *
 * @since 1.0.0
 */
@Component
public class FieldEncryptor {

    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final String PREFIX = "v1:";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    private final FieldEncryptionProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Creates an encryptor using field-encryption configuration properties.
     *
     * @param properties encryption configuration
     */
    public FieldEncryptor(FieldEncryptionProperties properties) {
        this.properties = properties;
    }

    /**
     * Encrypts a plain text value using AES-GCM.
     *
     * <p>Values that already use the encrypted value prefix are returned
     * unchanged so repeated persistence conversion remains idempotent.</p>
     *
     * @param plainText plain text value, or {@code null}
     * @return encrypted value, existing encrypted value, or {@code null}
     * @throws FieldEncryptionException if encryption is disabled, the key is
     *                                  invalid, or encryption fails
     */
    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        if (isEncrypted(plainText)) {
            return plainText;
        }

        byte[] iv = new byte[IV_BYTES];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return PREFIX + base64(iv) + ":" + base64(encrypted);
        } catch (GeneralSecurityException ex) {
            throw new FieldEncryptionException("Failed to encrypt field value", ex);
        }
    }

    /**
     * Decrypts an encrypted field value.
     *
     * <p>Plain values that do not use the encrypted value prefix are returned
     * unchanged to support nullable and legacy data.</p>
     *
     * @param cipherText encrypted field value, plain value, or {@code null}
     * @return decrypted plain text, unchanged plain value, or {@code null}
     * @throws FieldEncryptionException if the encrypted value is malformed, the
     *                                  key is invalid, or decryption fails
     */
    public String decrypt(String cipherText) {
        if (cipherText == null || !isEncrypted(cipherText)) {
            return cipherText;
        }

        String[] parts = cipherText.split(":", 3);
        if (parts.length != 3) {
            throw new FieldEncryptionException("Invalid encrypted field value format");
        }

        try {
            byte[] iv = Base64.getDecoder().decode(parts[1]);
            byte[] encrypted = Base64.getDecoder().decode(parts[2]);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException ex) {
            throw new FieldEncryptionException("Failed to decrypt field value", ex);
        }
    }

    /**
     * Indicates whether a value uses the encrypted field value format.
     *
     * @param value value to inspect
     * @return {@code true} when the value starts with the encryption prefix
     */
    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    private SecretKeySpec secretKey() {
        if (!properties.isEnabled()) {
            throw new FieldEncryptionException("Field encryption is disabled. Set app.field-encryption.enabled=true.");
        }
        if (properties.getKey() == null || properties.getKey().isBlank()) {
            throw new FieldEncryptionException("Field encryption key is empty. Set app.field-encryption.key.");
        }

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(properties.getKey());
        } catch (IllegalArgumentException ex) {
            throw new FieldEncryptionException("Field encryption key must be Base64 encoded", ex);
        }

        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new FieldEncryptionException("Field encryption key must decode to 16, 24, or 32 bytes");
        }
        return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }

    private static String base64(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }
}
