package com.motorph.inventory.test;

import com.motorph.inventory.DB;
import com.motorph.inventory.InventoryData;
import com.motorph.inventory.JdbcInventoryRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Date;

import static org.junit.Assert.*;

public class JdbcInventoryRepositoryTest {

    private JdbcInventoryRepository repo;

    @Before
    public void setup() {
        repo = new JdbcInventoryRepository();
    }

    @After
    public void cleanup() throws Exception {
        try (java.sql.Connection c = DB.getConnection(); java.sql.Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM audit_log");
            st.executeUpdate("DELETE FROM inventory");
        }
    }

    @Test
    public void insertAndFindRoundTripPersistsToFile() throws Exception {
        InventoryData d = new InventoryData("Toyota", "1234567890", new Date(), "On-hand", "New");
        repo.insert(d);

        InventoryData found = repo.findByEngineNumber("1234567890");
        assertNotNull(found);
        assertEquals("Toyota", found.getBrand());
        assertEquals("On-hand", found.getStatus());
        assertEquals("New", found.getLevel());

        File dbFile = new File("data/motorph_inventory.db");
        assertTrue("DB file should exist on disk for backup/restart", dbFile.exists());
    }

    @Test(expected = Exception.class)
    public void duplicateInsertFails() throws Exception {
        InventoryData d = new InventoryData("Toyota", "1234567890", new Date(), "On-hand", "New");
        repo.insert(d);
        repo.insert(new InventoryData("Honda", "1234567890", new Date(), "On-hand", "New"));
    }

    @Test
    public void bulkInsertAndReadBack() throws Exception {
        try (java.sql.Connection c = DB.getConnection(); java.sql.Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM audit_log");
            st.executeUpdate("DELETE FROM inventory");
        }
        int total = 2_000;
        for (int i = 0; i < total; i++) {
            String eng = String.format("%010d", i);
            repo.insert(new InventoryData("Brand" + i, eng, new java.util.Date(), "On-hand", "New"));
        }
        java.util.List<com.motorph.inventory.InventoryData> all = repo.findAllSortedByEngineNumber();
        logBanner("Repo Bulk Insert");
        System.out.println("Inserted : " + total);
        System.out.println("Fetched  : " + all.size());
        System.out.println("First    : " + all.get(0).getEngineNumber());
        System.out.println("Last     : " + all.get(all.size() - 1).getEngineNumber() + "\n");
        assertEquals(total, all.size());
    }

    private void logBanner(String title) {
        System.out.println("\n--- " + title + " ---");
    }

}
