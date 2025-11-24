package com.motorph.inventory;

import java.sql.*;
import java.util.*;

class JdbcInventoryRepository implements InventoryRepository {

    @Override
    public void insert(InventoryData d) throws Exception {
        String sql = "INSERT INTO inventory(engine_number, brand, date_entered, status, level) VALUES(?,?,?,?,?)";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.getEngineNumber());
            ps.setString(2, d.getBrand());
            ps.setLong(3, d.getDateEntered() != null ? d.getDateEntered().getTime() : System.currentTimeMillis());
            ps.setString(4, d.getStatus());
            ps.setString(5, d.getLevel());
            ps.executeUpdate();
        }
    }

    @Override
    public InventoryData findByEngineNumber(String engineNumber) throws Exception {
        String sql = "SELECT engine_number, brand, date_entered, status, level FROM inventory WHERE engine_number = ?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, engineNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    @Override
    public void update(InventoryData d) throws Exception {
        String sql = "UPDATE inventory SET brand=?, date_entered=?, status=?, level=? WHERE engine_number=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.getBrand());
            ps.setLong(2, d.getDateEntered() != null ? d.getDateEntered().getTime() : System.currentTimeMillis());
            ps.setString(3, d.getStatus());
            ps.setString(4, d.getLevel());
            ps.setString(5, d.getEngineNumber());
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteByEngineNumber(String engineNumber) throws Exception {
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM inventory WHERE engine_number=?")) {
            ps.setString(1, engineNumber);
            ps.executeUpdate();
        }
    }

    @Override
    public List<InventoryData> findAllSortedByEngineNumber() throws Exception {
        String sql = "SELECT engine_number, brand, date_entered, status, level FROM inventory ORDER BY engine_number ASC";
        List<InventoryData> out = new ArrayList<>();
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        }
        return out;
    }

    private InventoryData map(ResultSet rs) throws SQLException {
        String engine = rs.getString("engine_number");
        String brand = rs.getString("brand");
        java.util.Date entered = new java.util.Date(rs.getLong("date_entered"));
        String status = rs.getString("status");
        String level = rs.getString("level");
        return new InventoryData(brand, engine, entered, status, level);
    }
}