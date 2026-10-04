package model;

public class Admin extends User {
    public Admin(String id, String username, String passwordHash, String name) {
        super(id, username, passwordHash, name);
    }

    @Override public String getRole() { return "ADMIN"; }

    // id|username|passwordHash|name
    @Override public String toFileString() {
        return String.join("|", getId(), getUsername(), getPasswordHash(), getName());
    }

    public static Admin fromFileString(String line) {
        String[] f = line.split("\\|", -1);
        return new Admin(f[0], f[1], f[2], f[3]);
    }
}
