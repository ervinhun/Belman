package dk.easv.belman.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProbaTest {

    @Test
    void test1() {
        Proba p = new Proba();
        p.test();
        assertTrue(true);
    }
}