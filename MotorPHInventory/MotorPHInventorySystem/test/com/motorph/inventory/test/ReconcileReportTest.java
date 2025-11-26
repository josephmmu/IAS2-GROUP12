package com.motorph.inventory.test;

import com.motorph.inventory.AuditLogger;
import com.motorph.inventory.DB;
import com.motorph.inventory.InventoryData;
import com.motorph.inventory.JdbcInventoryRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class ReconcileReportTest {

    private JdbcInventoryRepository repo;
    private AuditLogger audit;

    @Before
    public void setup() throws Exception {
        repo = new JdbcInventoryRepository();
        audit = new AuditLogger();
        try (java.sql.Connection c = DB.getConnection(); java.sql.Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM audit_log");
            st.executeUpdate("DELETE FROM inventory");
        }
    }

    @After
    public void cleanup() throws Exception {
        try (java.sql.Connection c = DB.getConnection(); java.sql.Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM audit_log");
            st.executeUpdate("DELETE FROM inventory");
        }
    }

    @Test
    public void flagsInvalidAndInconsistentRecords() throws Exception {
        repo.insert(new InventoryData("Toyota", "12X", new Date(), "On-hand", "New"));            // bad engine
        repo.insert(new InventoryData("", "1234567899", null, "Broken", "Returned"));            // many issues
        repo.insert(new InventoryData("Ford", "1234567898", new Date(), "On-hand", "Sold"));      // inconsistent

        audit.reconcileAndReport("admin", repo);

        try (java.sql.Connection c = DB.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(
                     "SELECT details FROM audit_log WHERE action='RECONCILE' ORDER BY id DESC LIMIT 1");
             java.sql.ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertTrue(rs.getString(1).contains("issues="));
        }
    }

    @Test
    public void reconcileLargeDatasetWithScatteredIssues() throws Exception {
        // clear
        try (java.sql.Connection c = DB.getConnection(); java.sql.Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM audit_log");
            st.executeUpdate("DELETE FROM inventory");
        }
        int total = 3_000;
        for (int i = 0; i < total; i++) {
            String eng = String.format("%010d", i);
            String status = (i % 2 == 0) ? "On-hand" : "Old";
            String level = (i % 2 == 0) ? "New" : "Sold";
            repo.insert(new InventoryData("Brand" + i, eng, new java.util.Date(), status, level));
        }
        // Inject some bad rows
        repo.insert(new InventoryData("BadBrand", "12X", new java.util.Date(), "On-hand", "New"));
        repo.insert(new InventoryData("", "9999999999", null, "Broken", "Returned"));
        repo.insert(new InventoryData("Ford", "8888888888", new java.util.Date(), "On-hand", "Sold")); // inconsistent

        logBanner("Reconcile Stress");
        audit.reconcileAndReport("admin", repo);

        try (java.sql.Connection c = DB.getConnection();
            java.sql.PreparedStatement ps = c.prepareStatement(
                    "SELECT details FROM audit_log WHERE action='RECONCILE' ORDER BY id DESC LIMIT 1");
            java.sql.ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            String details = rs.getString(1);
            assertTrue(details.contains("issues="));
        }
    }

    private void logBanner(String title) {
    System.out.println("\n--- " + title + " ---");
}

}
