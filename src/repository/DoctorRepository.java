package repository;

import model.Doctor;

import java.util.*;

public class DoctorRepository {
    private static final String FILE = "doctors.txt";

    public List<Doctor> findAll() {
        List<Doctor> list = new ArrayList<>();
        for (String line : FileStore.read(FILE)) list.add(Doctor.fromFileString(line));
        return list;
    }

    public Optional<Doctor> findById(String id) {
        for (Doctor x : findAll()) if (x.getId().equals(id)) return Optional.of(x);
        return Optional.empty();
    }

    public void save(Doctor item) {
        List<Doctor> all = findAll();
        all.add(item);
        writeAll(all);
    }

    public void update(Doctor item) {
        List<Doctor> all = findAll();
        for (int i = 0; i < all.size(); i++)
            if (all.get(i).getId().equals(item.getId())) { all.set(i, item); writeAll(all); return; }
        throw new IllegalArgumentException("Doctor not found: " + item.getId());
    }

    public boolean delete(String id) {
        List<Doctor> all = findAll();
        boolean removed = all.removeIf(x -> x.getId().equals(id));
        if (removed) writeAll(all);
        return removed;
    }

    public String nextId() {
        List<String> ids = new ArrayList<>();
        for (Doctor x : findAll()) ids.add(x.getId());
        return FileStore.nextId("D", ids);
    }

    private void writeAll(List<Doctor> items) {
        List<String> lines = new ArrayList<>();
        for (Doctor x : items) lines.add(x.toFileString());
        FileStore.write(FILE, lines);
    }
}
