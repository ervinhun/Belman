package dk.easv.belman.bll;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHasher {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    public String hashPassword(String password, String stringForSalt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = generateSalt(stringForSalt);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    public byte[] generateSalt(String notSalted) {
        //Gets a string and generates a salt for hashing the password
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < notSalted.length(); i++) {
            char ch = notSalted.charAt(i);
            if (Character.isLetter(ch)) {
                if (i % 2 == 0) {
                    result.append(Character.toUpperCase(ch)); // Even index → Uppercase
                } else {
                    result.append(Character.toLowerCase(ch)); // Odd index → Lowercase
                }
            } else {
                result.append(ch); // Keep non-letter characters unchanged
            }
        }
        // Convert salt to bytes
        return result.toString().getBytes();
    }

    public void main(String[] args) throws Exception {
        String password = "yourSecurePassword";
        String salt = "myCustomSalt";

        String hashedPassword = hashPassword(password, salt);

        System.out.println("Salt: " + salt + " - " + generateSalt(salt).toString());
        System.out.println("Hashed Password: " + hashedPassword);
    }
}
