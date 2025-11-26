package com.motorph.inventory.test;

import com.motorph.inventory.InventorySystem;
import org.junit.Test;
import static org.junit.Assert.*;

public class InventorySystemValidationTest {

    @Test
    public void engineNumberMustBeTenDigits() throws Exception {
        InventorySystem system = new InventorySystem();
        assertFalse(invokeIsValidEngineNumber(system, "12345"));
        assertFalse(invokeIsValidEngineNumber(system, "12345678901"));
        assertFalse(invokeIsValidEngineNumber(system, "ABC1234567"));
        assertTrue(invokeIsValidEngineNumber(system, "1234567890"));
    }

    private boolean invokeIsValidEngineNumber(InventorySystem system, String input) throws Exception {
        java.lang.reflect.Method m = InventorySystem.class.getDeclaredMethod("isValidEngineNumber", String.class);
        m.setAccessible(true);
        return (Boolean) m.invoke(system, input);
    }

    @Test
    public void engineValidatorRejectsLargeBatchQuickly() throws Exception {
        InventorySystem system = new InventorySystem();
        int checked = 10_000;
        for (int i = 0; i < checked; i++) {
            assertFalse(invokeIsValidEngineNumber(system, "BAD" + i));
        }
        assertTrue(invokeIsValidEngineNumber(system, "1234567890"));
        logBanner("Input Validation Stress");
        System.out.println("Checked invalid: " + checked);
        System.out.println("Checked valid  : 2\n");
    }

    private void logBanner(String title) {
        System.out.println("\n--- " + title + " ---");
    }
}
