package com.motorph.inventory.test;

import com.motorph.inventory.AuthService;
import org.junit.Test;
import static org.junit.Assert.*;

public class AuthServiceTest {

    private final AuthService auth = new AuthService();

    @Test
    public void authenticateAcceptsValidUsers() {
        assertTrue(auth.authenticate("admin", "admin123"));
        assertTrue(auth.authenticate("user", "user123"));
    }

    @Test
    public void authenticateRejectsBadPasswordOrUnknownUser() {
        assertFalse(auth.authenticate("admin", "wrong"));
        assertFalse(auth.authenticate("nouser", "nopass"));
    }

    @Test
    public void getRoleDefaultsToUserForUnknown() {
        assertEquals("USER", auth.getRole("nouser"));
    }

    @Test
    public void isAdminTrueOnlyForAdmin() {
        assertTrue(auth.isAdmin("admin"));
        assertFalse(auth.isAdmin("user"));
        assertFalse(auth.isAdmin("nouser"));
    }

    @Test
    public void nonAdminDeleteDeniedEveryTime() {
        AuthService auth = new AuthService();
        for (int i = 0; i < 1_000; i++) {
            assertTrue(auth.authenticate("user", "user123"));
            assertFalse(auth.isAdmin("user"));
        }
    }

    @Test
    public void authenticateManyMixedAttempts() {
        AuthService auth = new AuthService();
        int ok = 0, fail = 0;
        for (int i = 0; i < 5_000; i++) {
            boolean result = ((i & 1) == 0)
                    ? auth.authenticate("admin", "admin123")
                    : auth.authenticate("admin", "wrong" + i);
            if (result) ok++; else fail++;
        }
        logBanner("Auth Stress");
        System.out.println("Attempts: 5000");
        System.out.println("Success : " + ok);
        System.out.println("Fail    : " + fail + "\n");
        assertEquals(2500, ok);
        assertEquals(2500, fail);
    }

    private void logBanner(String title) {
        System.out.println("\n--- " + title + " ---");
    }
}
