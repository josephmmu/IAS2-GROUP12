package com.motorph.inventory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class InventorySystem {
    private final InventoryBST bst;
    private final Scanner scanner;
    private final AuthService auth;
    private String currentUser;
    private String currentRole;
    private final InventoryRepository repo;
    private final AuditLogger audit;

    public InventorySystem() {
        bst = new InventoryBST();
        scanner = new Scanner(System.in);
        auth = new AuthService();
        repo = new JdbcInventoryRepository();
        audit = new AuditLogger();
    }

    public void start() {

        if (!authenticate()) {
            System.out.println("Exiting system.");
            return;
        }
        while (true) {
            displayMainMenu();
            System.out.print("Select an option: ");
            String option = scanner.nextLine();
            switch (option) {
                case "1" -> addStock();
                case "2" -> {
                    if (!isAdmin()) {
                        System.out.println("Access denied: Delete Stock is for Admins only.");
                        audit.log(currentUser, "DELETE", null, null, null, "DENIED", "Non-admin attempted delete"); // add
                        break;
                    }
                    deleteStock();
                }
                case "3" -> searchStock();
                case "4" -> displayInventory();
                case "5" -> {
                    System.out.println("Goodbye!  " + currentUser);
                    return;
                }
                case "6" -> audit.printRecent(50); // view audit log
                case "7" -> audit.reconcileAndReport(currentUser, repo); // reconciliation
                default -> System.out.println("Invalid option: Please enter a number between 1 and 7.");
            }
        }
    }

    private boolean authenticate() {
        System.out.println("=== MotorPH Inventory System ===");
        for (int attempts = 0; attempts < 3; attempts++) {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            if (auth.authenticate(username, password)) {
                currentUser = username;
                currentRole = auth.getRole(username);
                System.out.println("Login successful. Welcome, " + currentUser + "!");
                return true;
            } else {
                System.out.println("Login failed. Please check your username and password then try again.");
            }
        }
        System.out.println("Too many failed login attempts. Access denied. Please restart the system to try again.");
        return false;
    }

    private void displayMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Add Stock");
        System.out.println("2. Delete Stock" + (isAdmin() ? "" : " (Admin only)"));
        System.out.println("3. Search Inventory");
        System.out.println("4. Display Inventory (Sorted)");
        System.out.println("5. Exit ");
        System.out.println("6. View Audit Log");
        System.out.println("7. Reconciliation & Exception Report\n");
    }
    
    private boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(currentRole);
    }

    private boolean isValidEngineNumber(String value) {
        return value != null && value.matches("\\d{10}");
    }

    private void addStock() {
        String engineNumber = null; // for auditing on error
        try {
            System.out.print("\n\n\n\n\n\n\n\n\n\n\n\n\nEnter Engine Number (10 digits): ");
            engineNumber = scanner.nextLine().trim();
            while (!isValidEngineNumber(engineNumber)) {
                System.out.println("Invalid engine number. Must be exactly 10 digits (e.g., 1234567890). Please try again");
                System.out.print("Enter Engine Number (10 digits): ");
                engineNumber = scanner.nextLine().trim();
            }

            if (repo.findByEngineNumber(engineNumber) != null) {
                System.out.println("Engine number already exists in inventory. Please enter a unique value. \n");
                audit.log(currentUser, "ADD", engineNumber, null, null, "REJECTED", "Duplicate engine number");
                return;
            }

            System.out.print("Enter Brand: ");
            String brand = scanner.nextLine().trim();
            while (brand.isEmpty()) {
                System.out.println("Invalid input. Brand name cannot be empty. Please enter a valid brand.");
                System.out.print("Enter Brand: ");
                brand = scanner.nextLine().trim();
            }

            InventoryData newRecord = new InventoryData(brand, engineNumber, new Date(), "On-hand", "New");
            repo.insert(newRecord);
            System.out.println("Product added successfully:");
            bst.printRecordWithHeader(newRecord); // formatted single-record table
            audit.log(currentUser, "ADD", engineNumber, newRecord.getStatus(), newRecord.getLevel(), "SUCCESS", ""); // add
        } catch (Exception e) {
            System.out.println("Unexpected input error occured while adding product. Please check your entries and try again.");
            System.err.println("[addStock] " + e.getClass().getName() + ": " + e.getMessage());
            audit.logException(currentUser, "ADD", engineNumber, e); // add
        }
    }

    private void deleteStock() {
        try {
            while (true) {
                System.out.print("Enter Engine Number to delete (or type CANCEL to return): ");
                String engineNumber = scanner.nextLine().trim();
                if (engineNumber.equalsIgnoreCase("CANCEL")) {
                    System.out.println("Delete cancelled. Returning to main menu.");
                    audit.log(currentUser, "DELETE", null, null, null, "CANCEL", "User cancelled");
                    return;
                }
                if (!isValidEngineNumber(engineNumber)) {
                    System.out.println("Invalid engine number: Must be exactly 10 digits (e.g., 1234567890). Please try again.");
                    audit.log(currentUser, "DELETE", engineNumber, null, null, "REJECTED", "Invalid engine number format");
                    continue;
                }

                InventoryData record = repo.findByEngineNumber(engineNumber);
                if (record == null) {
                    System.out.println("No product found. Please verify the engine number and try again.");
                    audit.log(currentUser, "DELETE", engineNumber, null, null, "REJECTED", "Not found");
                    return;
                }
                if (!"On-hand".equalsIgnoreCase(record.getStatus())) {
                    System.out.println("Product cannot be deleted. Status must be 'On-hand'.");
                    audit.log(currentUser, "DELETE", engineNumber, record.getStatus(), record.getLevel(), "REJECTED", "Invalid status for delete");
                    return;
                }

                System.out.println("Product to delete:");
                bst.printRecordWithHeader(record);

                System.out.print("To confirm deletion, re-enter the Engine Number (or type CANCEL to abort): ");
                String confirmNumber = scanner.nextLine().trim();
                if (confirmNumber.equalsIgnoreCase("CANCEL")) {
                    System.out.println("Deletion cancelled.");
                    audit.log(currentUser, "DELETE", engineNumber, record.getStatus(), record.getLevel(), "CANCEL", "User cancelled at confirm");
                    return;
                }
                if (!isValidEngineNumber(confirmNumber)) {
                    System.out.println("Invalid confirmation: Engine number must be exactly 10 digits. Deletion cancelled.");
                    audit.log(currentUser, "DELETE", engineNumber, record.getStatus(), record.getLevel(), "REJECTED", "Invalid confirm format");
                    return;
                }

                if (confirmNumber.equals(engineNumber)) {
                    record.setStatus("Old");
                    record.setLevel("Sold");
                    repo.update(record);
                    repo.deleteByEngineNumber(engineNumber);
                    System.out.println("Product deleted successfully:");
                    bst.printRecordWithHeader(record);
                    audit.log(currentUser, "DELETE", engineNumber, record.getStatus(), record.getLevel(), "SUCCESS", "");
                } else {
                    System.out.println("Engine number mismatch. Deletion not confirmed and has been cancelled.");
                    audit.log(currentUser, "DELETE", engineNumber, record.getStatus(), record.getLevel(), "REJECTED", "Confirm mismatch");
                }
                return;
            }
        } catch (Exception e) {
            System.out.println("Unexpected error occurred during deletion. Operation cancelled.");
            System.err.println("[deleteStock] " + e.getClass().getName() + ": " + e.getMessage());
            audit.logException(currentUser, "DELETE", null, e);
        }
    }

    private void searchStock() {
        try {
            while (true) {
                System.out.print("Enter Engine Number to search (or type CANCEL to return): ");
                String engineNumber = scanner.nextLine().trim();
                if (engineNumber.equalsIgnoreCase("CANCEL")) {
                    System.out.println("Search cancelled. Returning to main menu.");
                    return;
                }
                if (!isValidEngineNumber(engineNumber)) {
                    System.out.println("Invalid engine number: Must be exactly 10 digits (e.g., 1234567890). Please try again.");
                    continue;
                }

                InventoryData record = repo.findByEngineNumber(engineNumber);
                if (record != null) {
                    System.out.println("Product found:");
                    bst.printRecordWithHeader(record);
                } else {
                    System.out.println("No product found with the given engine number. Please check and try again.");
                }
                return;
            }
        } catch (Exception e) {
            System.out.println("Unexpected error occurred during search. Please try again.");
            System.err.println("[searchStock] " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private static final String ROW_FORMAT = "%-15s %-12s %-16s %-10s %-10s";
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private void displayInventory() {
        System.out.println("Displaying inventory records sorted by Engine Number:");
        try {
            List<InventoryData> all = repo.findAllSortedByEngineNumber();
            if (all.isEmpty()) {
                System.out.println("(no records)");
                return;
            }
            String header = String.format(ROW_FORMAT, "Brand", "Engine No.", "Date Entered", "Status", "Level");
            System.out.println(header);
            System.out.println("-".repeat(header.length()));
            for (InventoryData d : all) {
                String date = d.getDateEntered() != null ? DATE_FMT.format(d.getDateEntered()) : "";
                System.out.printf(ROW_FORMAT + "%n",
                        safe(d.getBrand()), safe(d.getEngineNumber()), date, safe(d.getStatus()), safe(d.getLevel()));
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("Unable to display inventory.");
        }
    }

    private String safe(String s) { return s == null ? "" : s; }

}