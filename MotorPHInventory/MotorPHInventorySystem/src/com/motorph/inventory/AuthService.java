package com.motorph.inventory;

import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private final Map<String, String> userDatabase;

    private final Map<String, String> roles;

    public AuthService() {
        userDatabase = new HashMap<>();
        // Predefined users (username -> password)
        userDatabase.put("admin", "admin123");
        userDatabase.put("user", "user123");

        roles = new HashMap<>();
        roles.put("admin", "ADMIN");
        roles.put("user", "USER");
    }

    public boolean authenticate(String username, String password) {
        return userDatabase.containsKey(username) && userDatabase.get(username).equals(password);
    }

    public String getRole(String username) {
        return roles.getOrDefault(username, "USER");
    }

    public boolean isAdmin(String username) {
        return "ADMIN".equalsIgnoreCase(getRole(username));
    }
    
}
