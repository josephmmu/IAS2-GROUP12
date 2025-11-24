package com.motorph.inventory;

public class Main {
    public static void main(String[] args) {
        try {
            DB.ensureReady();
        } catch (RuntimeException e) {
            System.err.println("Startup failed: " + e.getMessage());
            System.err.println("Ensure the SQLite driver JAR is on the classpath.");
            System.err.println("See: nbproject/project.properties -> javac.classpath");
            System.exit(1);
        }

        InventorySystem system = new InventorySystem();
        system.start();
    }
}
