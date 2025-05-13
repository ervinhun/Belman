package dk.easv.belman.bll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    private PasswordHasher hasher;

    @BeforeEach
    void setUp() {
        hasher = new PasswordHasher();
    }

    @Test
    void testHashPassword_NotNull() throws Exception {
        String hash = hasher.hashPassword("test123", "salt123");
        assertNotNull(hash, "Hashed password should not be null");
    }

    @Test
    void testSamePasswordSameSaltSameHash() throws Exception {
        String password = "securePassword";
        String salt = "mySalt";

        String hash1 = hasher.hashPassword(password, salt);
        String hash2 = hasher.hashPassword(password, salt);

        assertEquals(hash1, hash2, "Same password and salt must produce the same hash");
    }

    @Test
    void testDifferentPasswordDifferentHash() throws Exception {
        String salt = "sharedSalt";

        String hash1 = hasher.hashPassword("firstPass", salt);
        String hash2 = hasher.hashPassword("secondPass", salt);

        assertNotEquals(hash1, hash2, "Different passwords should produce different hashes");
    }

    @Test
    void testHashNotEqualToPlainPassword() throws Exception {
        String password = "plaintext";
        String hash = hasher.hashPassword(password, "salt");

        assertNotEquals(password, hash, "Hash should not match the original password");
    }

    @Test
    void testGenerateSalt_Deterministic() {
        byte[] salt1 = hasher.generateSalt("testInput");
        byte[] salt2 = hasher.generateSalt("testInput");

        assertArrayEquals(salt1, salt2, "Same input must produce same salt bytes");
    }

    @Test
    void testGenerateSaltFormat() {
        String input = "a1b2C3!";
        byte[] result = hasher.generateSalt(input);
        String saltString = new String(result);

        assertEquals("A1B2C3!", saltString, "Salt format must match the expected logic");
    }

    @Test
    void testGenerateSalt_LongInput() {
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) longInput.append("a1");

        byte[] salt = hasher.generateSalt(longInput.toString());
        assertEquals(2000, salt.length, "Salt should be same length as input");
    }

    @Test
    void testGenerateSaltWithUnicode() {
        String unicodeInput = "Pa$$w☠️rd";
        byte[] salt = hasher.generateSalt(unicodeInput);

        assertNotNull(salt);
        assertTrue(salt.length > 0);
    }
}
