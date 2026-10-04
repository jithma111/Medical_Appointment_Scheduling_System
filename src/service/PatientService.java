package service;

import model.*;
import repository.*;

import java.util.*;

public class PatientService {
    private final PatientRepository patients;
    private final UserRepository users;

    public PatientService(PatientRepository patients, UserRepository users) {
        this.patients = patients; this.users = users;
    }

    public Patient register(String name, int age, String phone, String username, String password) {
        name = FileStore.clean(name); username = FileStore.clean(username);
        if (name.isEmpty()) throw new IllegalArgumentException("Name is required");
        if (age < 0 || age > 120) throw new IllegalArgumentException("Age must be between 0 and 120");
        validatePhone(phone);
        if (!username.matches("[A-Za-z0-9_]{4,20}")) throw new IllegalArgumentException("Username: 4-20 letters, digits or _");
        validatePassword(password);
        if (users.usernameExists(username)) throw new IllegalArgumentException("Username already taken");

        Patient p = new Patient(patients.nextId(), username, User.hash(password), name, age, phone);
        patients.save(p);
        return p;
    }

    public Patient updateProfile(String id, String phone, String newPassword) {
        Patient p = get(id);
        validatePhone(phone);
        p.setPhone(phone);
        if (newPassword != null && !newPassword.isEmpty()) { validatePassword(newPassword); p.setPassword(newPassword); }
        patients.update(p);
        return p;
    }

    public Optional<Patient> findById(String id) { return patients.findById(id); }
    public List<Patient> findAll() { return patients.findAll(); }

    public Patient get(String id) {
        return patients.findById(id).orElseThrow(() -> new IllegalArgumentException("Patient not found"));
    }

    public void delete(String id) {
        if (!patients.delete(id)) throw new IllegalArgumentException("Patient not found");
    }

    private void validatePhone(String phone) {
        if (phone == null || !phone.matches("0\\d{9}")) throw new IllegalArgumentException("Phone must be 10 digits starting with 0");
    }

    private void validatePassword(String pw) {
        if (pw == null || pw.length() < 6) throw new IllegalArgumentException("Password must be at least 6 characters");
    }
}
