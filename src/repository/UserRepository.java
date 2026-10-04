package repository;

import model.*;

import java.util.*;

/** Looks up login accounts across admins, patients and doctors. Admins are stored here (admins.txt). */
public class UserRepository {
    private static final String ADMIN_FILE = "admins.txt";
    private final PatientRepository patients;
    private final DoctorRepository doctors;

    public UserRepository(PatientRepository patients, DoctorRepository doctors) {
        this.patients = patients; this.doctors = doctors;
        seedDefaultAdmin();
    }

    public List<Admin> findAdmins() {
        List<Admin> list = new ArrayList<>();
        for (String line : FileStore.read(ADMIN_FILE)) list.add(Admin.fromFileString(line));
        return list;
    }

    public Optional<User> findByUsername(String username) {
        for (Admin a : findAdmins()) if (a.getUsername().equalsIgnoreCase(username)) return Optional.of(a);
        for (Patient p : patients.findAll()) if (p.getUsername().equalsIgnoreCase(username)) return Optional.of(p);
        for (Doctor d : doctors.findAll()) if (d.getUsername().equalsIgnoreCase(username)) return Optional.of(d);
        return Optional.empty();
    }

    public boolean usernameExists(String username) { return findByUsername(username).isPresent(); }

    /** First run: creates admin / admin123 so someone can log in and add doctors. */
    private void seedDefaultAdmin() {
        if (FileStore.read(ADMIN_FILE).isEmpty()) {
            Admin a = new Admin("AD001", "admin", User.hash("admin123"), "System Admin");
            FileStore.write(ADMIN_FILE, Collections.singletonList(a.toFileString()));
        }
    }
}
