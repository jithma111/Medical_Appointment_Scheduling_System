package model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Base class for everyone who can log in. */
public abstract class User {
    private final String id;
    private String username;
    private String passwordHash;
    private String name;

    protected User(String id, String username, String passwordHash, String name) {
        this.id = id; this.username = username; this.passwordHash = passwordHash; this.name = name;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public void setPassword(String rawPassword) { this.passwordHash = hash(rawPassword); }
    public boolean checkPassword(String rawPassword) { return passwordHash.equals(hash(rawPassword)); }

    public static String hash(String raw) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public abstract String getRole();
    public abstract String toFileString();
}
