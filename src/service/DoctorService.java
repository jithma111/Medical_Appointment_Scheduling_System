package service;

import model.*;
import repository.*;

import java.util.*;

public class DoctorService {
    private static final String SLOT_PATTERN = "([01]\\d|2[0-3]):[0-5]\\d";
    private final DoctorRepository doctors;
    private final UserRepository users;

    public DoctorService(DoctorRepository doctors, UserRepository users) {
        this.doctors = doctors; this.users = users;
    }

    public Doctor addDoctor(String name, String specialization, double fee, String username,
                            String password, String slotsCsv) {
        name = FileStore.clean(name); specialization = FileStore.clean(specialization); username = FileStore.clean(username);
        if (name.isEmpty() || specialization.isEmpty()) throw new IllegalArgumentException("Name and specialization are required");
        if (fee < 0) throw new IllegalArgumentException("Fee cannot be negative");
        if (!username.matches("[A-Za-z0-9_]{4,20}")) throw new IllegalArgumentException("Username: 4-20 letters, digits or _");
        if (password == null || password.length() < 6) throw new IllegalArgumentException("Password must be at least 6 characters");
        if (users.usernameExists(username)) throw new IllegalArgumentException("Username already taken");

        Doctor d = new Doctor(doctors.nextId(), username, User.hash(password), name, specialization, fee, parseSlots(slotsCsv));
        doctors.save(d);
        return d;
    }

    public Doctor updateSlots(String id, String slotsCsv) {
        Doctor d = get(id);
        d.setAvailableSlots(parseSlots(slotsCsv));
        doctors.update(d);
        return d;
    }

    public Doctor updateFee(String id, double fee) {
        if (fee < 0) throw new IllegalArgumentException("Fee cannot be negative");
        Doctor d = get(id);
        d.setFee(fee);
        doctors.update(d);
        return d;
    }

    /** Blank keyword returns everyone; otherwise matches name or specialization. */
    public List<Doctor> search(String keyword) {
        List<Doctor> all = doctors.findAll();
        if (keyword == null || keyword.trim().isEmpty()) return all;
        String k = keyword.trim().toLowerCase();
        List<Doctor> out = new ArrayList<>();
        for (Doctor d : all)
            if (d.getName().toLowerCase().contains(k) || d.getSpecialization().toLowerCase().contains(k)) out.add(d);
        return out;
    }

    public Optional<Doctor> findById(String id) { return doctors.findById(id); }
    public List<Doctor> findAll() { return doctors.findAll(); }

    public Doctor get(String id) {
        return doctors.findById(id).orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
    }

    public void delete(String id) {
        if (!doctors.delete(id)) throw new IllegalArgumentException("Doctor not found");
    }

    private List<String> parseSlots(String csv) {
        Set<String> slots = new TreeSet<>();
        if (csv != null) for (String s : csv.split(",")) {
            s = s.trim();
            if (s.isEmpty()) continue;
            if (!s.matches(SLOT_PATTERN)) throw new IllegalArgumentException("Invalid slot '" + s + "' (use HH:mm, e.g. 09:30)");
            slots.add(s);
        }
        if (slots.isEmpty()) throw new IllegalArgumentException("At least one time slot is required");
        return new ArrayList<>(slots);
    }
}
