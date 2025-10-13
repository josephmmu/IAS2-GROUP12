package com.motorph.inventory;

import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private final Map<String, String> userDatabase;

    public AuthService() {
        userDatabase = new HashMap<>();
        // Predefined users (username -> password)
        userDatabase.put("admin", "admin123");
        userDatabase.put("user", "user123");
    }

    public boolean authenticate(String username, String password) {
        return userDatabase.containsKey(username) && userDatabase.get(username).equals(password);
    }
    
}
