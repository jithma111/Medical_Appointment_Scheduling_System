package model;

import java.util.*;

public class Doctor extends User {
    private String specialization;
    private double fee;
    private List<String> availableSlots;   // daily time slots, e.g. 09:00

    public Doctor(String id, String username, String passwordHash, String name,
                  String specialization, double fee, List<String> availableSlots) {
        super(id, username, passwordHash, name);
        this.specialization = specialization; this.fee = fee;
        this.availableSlots = new ArrayList<>(availableSlots);
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String s) { this.specialization = s; }
    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }
    public List<String> getAvailableSlots() { return new ArrayList<>(availableSlots); }
    public void setAvailableSlots(List<String> slots) { this.availableSlots = new ArrayList<>(slots); }

    @Override public String getRole() { return "DOCTOR"; }

    // id|username|passwordHash|name|specialization|fee|slot1,slot2
    @Override public String toFileString() {
        return String.join("|", getId(), getUsername(), getPasswordHash(), getName(),
                specialization, String.valueOf(fee), String.join(",", availableSlots));
    }

    public static Doctor fromFileString(String line) {
        String[] f = line.split("\\|", -1);
        List<String> slots = f[6].isEmpty() ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(f[6].split(",")));
        return new Doctor(f[0], f[1], f[2], f[3], f[4], Double.parseDouble(f[5]), slots);
    }
}
