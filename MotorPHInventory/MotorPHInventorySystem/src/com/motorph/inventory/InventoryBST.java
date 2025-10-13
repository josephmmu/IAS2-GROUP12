package com.motorph.inventory;

public class InventoryBST {
    private class Node {
        InventoryData data;
        Node left, right;

        Node(InventoryData data) {
            this.data = data;
            left = right = null;
        }
    }

    private Node root;

    // Pretty-print helpers for inOrder()
    private static final String ROW_FORMAT = "%-15s %-12s %-16s %-10s %-10s";
    private static final java.text.SimpleDateFormat DATE_FMT =
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");

    private void printHeader() {
        String header = String.format(ROW_FORMAT, "Brand", "Engine No.", "Date Entered", "Status", "Level");
        System.out.println(header);
        System.out.println(lineOf(header.length()));
    }

    private void printRow(InventoryData d) {
        String date = d.getDateEntered() != null ? DATE_FMT.format(d.getDateEntered()) : "";
        System.out.printf(ROW_FORMAT + "%n",
                safe(d.getBrand()),
                safe(d.getEngineNumber()),
                date,
                safe(d.getStatus()),
                safe(d.getLevel()));
    }

    private String lineOf(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append('-');
        return sb.toString();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    public void insert(InventoryData data) {
        root = insertRec(root, data);
    }

    private Node insertRec(Node root, InventoryData data) {
        if (root == null) {
            return new Node(data);
        }
        if (data.getEngineNumber().compareTo(root.data.getEngineNumber()) < 0) {
            root.left = insertRec(root.left, data);
        } else if (data.getEngineNumber().compareTo(root.data.getEngineNumber()) > 0) {
            root.right = insertRec(root.right, data);
        }
        return root;
    }

    public InventoryData search(String engineNumber) {
        Node node = searchRec(root, engineNumber);
        return (node != null) ? node.data : null;
    }

    private Node searchRec(Node root, String engineNumber) {
        if (root == null || root.data.getEngineNumber().equals(engineNumber)) {
            return root;
        }
        if (engineNumber.compareTo(root.data.getEngineNumber()) < 0) {
            return searchRec(root.left, engineNumber);
        }
        return searchRec(root.right, engineNumber);
    }

    public void delete(String engineNumber) {
        root = deleteRec(root, engineNumber);
    }

    private Node deleteRec(Node root, String engineNumber) {
        if (root == null) {
            return root;
        }
        if (engineNumber.compareTo(root.data.getEngineNumber()) < 0) {
            root.left = deleteRec(root.left, engineNumber);
        } else if (engineNumber.compareTo(root.data.getEngineNumber()) > 0) {
            root.right = deleteRec(root.right, engineNumber);
        } else {
            if (root.left == null) {
                return root.right;
            } else if (root.right == null) {
                return root.left;
            }
            root.data = minValue(root.right);
            root.right = deleteRec(root.right, root.data.getEngineNumber());
        }
        return root;
    }

    private InventoryData minValue(Node root) {
        InventoryData minv = root.data;
        while (root.left != null) {
            minv = root.left.data;
            root = root.left;
        }
        return minv;
    }

    public void inOrder() {
        if (root == null) {
            System.out.println("(no records)");
            return;
        }
        printHeader();
        inOrderRec(root);
        System.out.println(); // spacer after table
    }

    // Public helper to print a single record using the same table formatting
    public void printRecordWithHeader(InventoryData d) {
        if (d == null) {
            System.out.println("(no record)");
            return;
        }
        printHeader();
        printRow(d);
        System.out.println(); // spacer after table
    }

    private void inOrderRec(Node root) {
        if (root != null) {
            inOrderRec(root.left);
            printRow(root.data);
            inOrderRec(root.right);
        }
    }
}