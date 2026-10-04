package model;

public class Patient extends User {
    private int age;
    private String phone;

    public Patient(String id, String username, String passwordHash, String name, int age, String phone) {
        super(id, username, passwordHash, name);
        this.age = age; this.phone = phone;
    }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override public String getRole() { return "PATIENT"; }

    // id|username|passwordHash|name|age|phone
    @Override public String toFileString() {
        return String.join("|", getId(), getUsername(), getPasswordHash(), getName(), String.valueOf(age), phone);
    }

    public static Patient fromFileString(String line) {
        String[] f = line.split("\\|", -1);
        return new Patient(f[0], f[1], f[2], f[3], Integer.parseInt(f[4]), f[5]);
    }
}
