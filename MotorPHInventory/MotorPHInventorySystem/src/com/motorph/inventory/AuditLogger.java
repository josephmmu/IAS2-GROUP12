package com.motorph.inventory;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class AuditLogger {
    private static final SimpleDateFormat TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void log(String user, String action, String engineNumber, String status, String level, String outcome, String details) {
        String sql = "INSERT INTO audit_log(ts, user, action, engine_number, status, level, outcome, details) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, System.currentTimeMillis());
            ps.setString(2, user);
            ps.setString(3, action);
            ps.setString(4, engineNumber);
            ps.setString(5, status);
            ps.setString(6, level);
            ps.setString(7, outcome);
            ps.setString(8, details);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("[audit] " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    public void logException(String user, String action, String engineNumber, Exception ex) {
        log(user, action, engineNumber, null, null, "ERROR", ex.getClass().getSimpleName() + ": " + ex.getMessage());
    }

    public void printRecent(int limit) {
        String sql = """
            SELECT ts, user, action, engine_number, outcome, details
            FROM audit_log
            ORDER BY id DESC
            LIMIT ?
        """;
        System.out.println("\n--- Audit Log (latest " + limit + ") ---");
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("(no audit entries)\n");
                    return;
                }
                while (rs.next()) {
                    String when = TS.format(new Date(rs.getLong("ts")));
                    String user = Optional.ofNullable(rs.getString("user")).orElse("-");
                    String action = rs.getString("action");
                    String eng = Optional.ofNullable(rs.getString("engine_number")).orElse("-");
                    String outcome = rs.getString("outcome");
                    String details = Optional.ofNullable(rs.getString("details")).orElse("");
                    System.out.printf("[%s] %-10s u=%-10s eng=%-12s -> %-8s %s%n",
                            when, action, user, eng, outcome, details);
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("Unable to display audit log.");
        }
    }

    // Reconciliation & Exception reporting:
    // Validates simple business rules and prints a report; also logs a summary row.
    public void reconcileAndReport(String user, InventoryRepository repo) {
        final Set<String> ALLOWED_STATUS = Set.of("On-hand", "Old");
        final Set<String> ALLOWED_LEVEL  = Set.of("New", "Sold");

        List<String> issues = new ArrayList<>();
        int scanned = 0;
        try {
            List<InventoryData> all = repo.findAllSortedByEngineNumber();
            scanned = all.size();
            for (InventoryData d : all) {
                String eng = d.getEngineNumber();
                String brand = d.getBrand();
                String status = d.getStatus();
                String level = d.getLevel();

                if (eng == null || !eng.matches("\\d{10}")) {
                    issues.add(eng + " -> invalid engine number format");
                }
                if (brand == null || brand.isBlank()) {
                    issues.add(eng + " -> missing brand");
                }
                if (d.getDateEntered() == null) {
                    issues.add(eng + " -> missing date_entered");
                }
                if (status == null || !ALLOWED_STATUS.contains(status)) {
                    issues.add(eng + " -> invalid status '" + status + "'");
                }
                if (level == null || !ALLOWED_LEVEL.contains(level)) {
                    issues.add(eng + " -> invalid level '" + level + "'");
                }
                // Cross-field consistency rules (customizable)
                if ("On-hand".equalsIgnoreCase(status) && "Sold".equalsIgnoreCase(level)) {
                    issues.add(eng + " -> inconsistent: On-hand cannot be Sold");
                }
                if ("Old".equalsIgnoreCase(status) && "New".equalsIgnoreCase(level)) {
                    issues.add(eng + " -> inconsistent: Old cannot be New");
                }
            }

            // Print report
            System.out.println("\n--- Reconciliation & Exception Report ---");
            System.out.println("Records scanned: " + scanned);
            System.out.println("Issues found  : " + issues.size());
            if (issues.isEmpty()) {
                System.out.println("No inconsistencies detected.\n");
            } else {
                for (String i : issues) System.out.println(" - " + i);
                System.out.println();
            }

            // Log summary
            log(user, "RECONCILE", null, null, null, "SUCCESS",
                    "scanned=" + scanned + ", issues=" + issues.size());
        } catch (Exception e) {
            System.out.println("Reconciliation failed. See error log.");
            logException(user, "RECONCILE", null, e);
        }
    }
}