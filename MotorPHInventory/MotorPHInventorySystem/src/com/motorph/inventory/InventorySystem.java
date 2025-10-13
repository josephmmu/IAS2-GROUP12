package com.motorph.inventory;

import java.util.Date;
import java.util.Scanner;

public class InventorySystem {
    private final InventoryBST bst;
    private final Scanner scanner;
    private final AuthService auth;           // add
    private String currentUser;   

    public InventorySystem() {
        bst = new InventoryBST();
        scanner = new Scanner(System.in);
        auth = new AuthService();  // add
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
                case "2" -> deleteStock();
                case "3" -> searchStock();
                case "4" -> displayInventory();
                case "5" -> {
                    System.out.println("Goodbye!  " + currentUser);
                    return;
                }
                default -> System.out.println("Invalid option, please try again.");
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
                System.out.println("Login successful. Welcome, " + currentUser + "!");
                return true;
            } else {
                System.out.println("Invalid credentials. Please try again.");
            }
        }
        System.out.println("Too many failed attempts.");
        return false;
    }

    private void displayMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Add Stock");
        System.out.println("2. Delete Stock");
        System.out.println("3. Search Inventory");
        System.out.println("4. Display Inventory (Sorted)");
        System.out.println("5. Exit \n");
    }
    

    private boolean isValidEngineNumber(String value) {
        return value != null && value.matches("\\d{10}");
    }

    private void addStock() {
        try {
            System.out.print("\n\n\n\n\n\n\n\n\n\n\n\n\nEnter Engine Number (10 digits): ");
            String engineNumber = scanner.nextLine().trim();
            while (!isValidEngineNumber(engineNumber)) {
                System.out.println("Invalid engine number. Enter exactly 10 digits (0-9).");
                System.out.print("Enter Engine Number (10 digits): ");
                engineNumber = scanner.nextLine().trim();
            }

            if (bst.search(engineNumber) != null) {
                System.out.println("Duplicate engine number.");
                return;
            }

            System.out.print("Enter Brand: ");
            String brand = scanner.nextLine().trim();
            while (brand.isEmpty()) {
                System.out.println("Brand cannot be empty.");
                System.out.print("Enter Brand: ");
                brand = scanner.nextLine().trim();
            }

            InventoryData newRecord = new InventoryData(brand, engineNumber, new Date(), "On-hand", "New");
            bst.insert(newRecord);
            System.out.println("Product added successfully:");
            bst.printRecordWithHeader(newRecord); // formatted single-record table
        } catch (Exception e) {
            System.out.println("Input error. Please try again.");
        }
    }

    private void deleteStock() {
        try {
            while (true) {
                System.out.print("Enter Engine Number to delete (or type CANCEL to return): ");
                String engineNumber = scanner.nextLine().trim();
                if (engineNumber.equalsIgnoreCase("CANCEL")) {
                    System.out.println("Delete cancelled. Returning to main menu.");
                    return;
                }
                if (!isValidEngineNumber(engineNumber)) {
                    System.out.println("Invalid engine number format. Must be exactly 10 digits.");
                    continue;
                }

                InventoryData record = bst.search(engineNumber);
                if (record == null) {
                    System.out.println("== Product not found ==\n");
                    return;
                }
                if (!"On-hand".equalsIgnoreCase(record.getStatus())) {
                    System.out.println("Product cannot be deleted (status is not 'On-hand').");
                    return;
                }

                System.out.println("Product to delete:");
                bst.printRecordWithHeader(record);

                System.out.print("To confirm deletion, re-enter the Engine Number (or type CANCEL to abort): ");
                String confirmNumber = scanner.nextLine().trim();
                if (confirmNumber.equalsIgnoreCase("CANCEL")) {
                    System.out.println("Deletion cancelled.");
                    return;
                }
                if (!isValidEngineNumber(confirmNumber)) {
                    System.out.println("Invalid confirmation format. Deletion cancelled.");
                    return;
                }

                if (confirmNumber.equals(engineNumber)) {
                    record.setStatus("Old");
                    record.setLevel("Sold");
                    bst.delete(engineNumber);
                    System.out.println("Product deleted successfully:");
                    bst.printRecordWithHeader(record);
                } else {
                    System.out.println("Confirmation failed. Deletion cancelled.");
                }
                return;
            }
        } catch (Exception e) {
            System.out.println("Input error. Deletion cancelled.");
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
                    System.out.println("Invalid engine number format. Must be exactly 10 digits.");
                    continue;
                }

                InventoryData record = bst.search(engineNumber);
                if (record != null) {
                    System.out.println("Product found:");
                    bst.printRecordWithHeader(record);
                } else {
                    System.out.println("Product not found.");
                }
                return;
            }
        } catch (Exception e) {
            System.out.println("Input error. Please try again.");
        }
    }

    private void displayInventory() {
        System.out.println("Inventory (sorted by Engine Number):");
        bst.inOrder();
    }

}