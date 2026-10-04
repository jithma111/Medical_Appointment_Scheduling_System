package repository;

import model.Patient;

import java.util.*;

public class PatientRepository {
    private static final String FILE = "patients.txt";

    public List<Patient> findAll() {
        List<Patient> list = new ArrayList<>();
        for (String line : FileStore.read(FILE)) list.add(Patient.fromFileString(line));
        return list;
    }

    public Optional<Patient> findById(String id) {
        for (Patient x : findAll()) if (x.getId().equals(id)) return Optional.of(x);
        return Optional.empty();
    }

    public void save(Patient item) {
        List<Patient> all = findAll();
        all.add(item);
        writeAll(all);
    }

    public void update(Patient item) {
        List<Patient> all = findAll();
        for (int i = 0; i < all.size(); i++)
            if (all.get(i).getId().equals(item.getId())) { all.set(i, item); writeAll(all); return; }
        throw new IllegalArgumentException("Patient not found: " + item.getId());
    }

    public boolean delete(String id) {
        List<Patient> all = findAll();
        boolean removed = all.removeIf(x -> x.getId().equals(id));
        if (removed) writeAll(all);
        return removed;
    }

    public String nextId() {
        List<String> ids = new ArrayList<>();
        for (Patient x : findAll()) ids.add(x.getId());
        return FileStore.nextId("P", ids);
    }

    private void writeAll(List<Patient> items) {
        List<String> lines = new ArrayList<>();
        for (Patient x : items) lines.add(x.toFileString());
        FileStore.write(FILE, lines);
    }
}
