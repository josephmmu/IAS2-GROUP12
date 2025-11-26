package com.motorph.inventory.test;

import com.motorph.inventory.AuditLogger;
import com.motorph.inventory.DB;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

public class AuditLoggerTest {

    private AuditLogger audit;

    @Before
    public void setup() throws Exception {
        audit = new AuditLogger();
        try (java.sql.Connection c = DB.getConnection(); java.sql.Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM audit_log");
        }
    }

    @After
    public void tearDown() throws Exception {
        try (java.sql.Connection c = DB.getConnection(); java.sql.Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM audit_log");
        }
    }

    @Test
    public void logsSuccessAndErrorOutcomes() throws Exception {
        audit.log("user1", "ADD", "1234567890", "On-hand", "New", "SUCCESS", "");
        audit.logException("user1", "ADD", "1234567890", new RuntimeException("boom"));

        try (java.sql.Connection c = DB.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement("SELECT outcome FROM audit_log ORDER BY id ASC");
             java.sql.ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertEquals("SUCCESS", rs.getString(1));
            assertTrue(rs.next());
            assertEquals("ERROR", rs.getString(1));
        }
    }

    @Test
    public void printRecentShowsLatestFirst() throws Exception {
        audit.log("user1", "ADD", "1234567890", "On-hand", "New", "SUCCESS", "");
        audit.log("user1", "DELETE", "1234567890", "Old", "Sold", "SUCCESS", ""); // latest

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(baos));
        try {
            audit.printRecent(10);
        } finally {
            System.setOut(original);
        }
        String out = baos.toString();
        assertTrue(out.contains("ADD"));
        assertTrue(out.contains("DELETE"));
        assertTrue(out.indexOf("DELETE") < out.indexOf("ADD")); // latest first
    }

    @Test
    public void logHighVolumeEntries() throws Exception {
        try (java.sql.Connection c = DB.getConnection(); java.sql.Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM audit_log");
        }
        int total = 5_000;
        for (int i = 0; i < total; i++) {
            audit.log("user" + (i % 5), "ADD", String.format("%010d", i), "On-hand", "New", "SUCCESS", "ok");
        }
        int count;
        try (java.sql.Connection c = DB.getConnection();
            java.sql.PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM audit_log");
            java.sql.ResultSet rs = ps.executeQuery()) {
            rs.next();
            count = rs.getInt(1);
        }
        logBanner("Audit Log Stress");
        System.out.println("Logged rows : " + total);
        System.out.println("DB count    : " + count + "\n");
        assertEquals(total, count);
    }

    private void logBanner(String title) {
        System.out.println("\n--- " + title + " ---");
    }

}
