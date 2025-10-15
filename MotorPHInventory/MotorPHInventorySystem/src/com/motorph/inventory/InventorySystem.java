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
                default -> System.out.println("Invalid option: Please enter a number between 1 and 5.");
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
                System.out.println("Login failed. Please check your username and password then try again.");
            }
        }
        System.out.println("Too many failed login attempts. Access denied. Please restart the system to try again.");
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
                System.out.println("Invalid engine number. Must be exactly 10 digits (e.g., 1234567890). Please try again");
                System.out.print("Enter Engine Number (10 digits): ");
                engineNumber = scanner.nextLine().trim();
            }

            if (bst.search(engineNumber) != null) {
                System.out.println("Engine number already exists in inventory. Please enter a unique value. \n");
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
            bst.insert(newRecord);
            System.out.println("Product added successfully:");
            bst.printRecordWithHeader(newRecord); // formatted single-record table
        } catch (Exception e) {
            System.out.println("Unexpected input error occured while adding product. Please check your entries and try again.");
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
                    System.out.println("Invalid engine number: Must be exactly 10 digits (e.g., 1234567890). Please try again.");
                    continue;
                }

                InventoryData record = bst.search(engineNumber);
                if (record == null) {
                    System.out.println("No product found. Please verify the engine number and try again.");
                    return;
                }
                if (!"On-hand".equalsIgnoreCase(record.getStatus())) {
                    System.out.println("Product cannot be deleted. Status must be 'On-hand'.");
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
                    System.out.println("Invalid confirmation: Engine number must be exactly 10 digits. Deletion cancelled.");
                    return;
                }

                if (confirmNumber.equals(engineNumber)) {
                    record.setStatus("Old");
                    record.setLevel("Sold");
                    bst.delete(engineNumber);
                    System.out.println("Product deleted successfully:");
                    bst.printRecordWithHeader(record);
                } else {
                    System.out.println("Engine number mismatch. Deletion not confirmed and has been cancelled.");
                }
                return;
            }
        } catch (Exception e) {
            System.out.println("Unexpected error occurred during deletion. Operation cancelled.");
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

                InventoryData record = bst.search(engineNumber);
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
        }
    }

    private void displayInventory() {
        System.out.println("Displaying inventory records sorted by Engine Number:");
        bst.inOrder();
    }

}