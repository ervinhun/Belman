package dk.easv.belman.config;

import dk.easv.belman.exceptions.BelmanException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;

public class ConfigCrypto {
    private static final String ALGORITHM = "AES";
    private static final String CONFIG_FILE = "config.properties";
    private static final String SECRET_KEY_PROPERTY = "encryption.key";
    private static final String SECRET_KEY;

    static {
        try {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
                props.load(in);
            }
            String key = props.getProperty(SECRET_KEY_PROPERTY);
            if (key == null || key.length() != 16) {
                throw new BelmanException("Encryption key must be exactly 16 characters long");
            }
            SECRET_KEY = key;
        } catch (IOException e) {
            throw new BelmanException("Failed to load encryption key from config.properties" + e);
        }
    }

    private ConfigCrypto() {
        throw new IllegalStateException("Utility class");
    }

    public static String encrypt(String input) throws Exception {
        Objects.requireNonNull(input, "Input cannot be null");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encrypted) throws Exception {
        Objects.requireNonNull(encrypted, "Encrypted input cannot be null");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decoded = Base64.getDecoder().decode(encrypted);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
