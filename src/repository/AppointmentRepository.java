package repository;

import model.Appointment;

import java.util.*;

public class AppointmentRepository {
    private static final String FILE = "appointments.txt";

    public List<Appointment> findAll() {
        List<Appointment> list = new ArrayList<>();
        for (String line : FileStore.read(FILE)) list.add(Appointment.fromFileString(line));
        return list;
    }

    public Optional<Appointment> findById(String id) {
        for (Appointment x : findAll()) if (x.getId().equals(id)) return Optional.of(x);
        return Optional.empty();
    }

    public void save(Appointment item) {
        List<Appointment> all = findAll();
        all.add(item);
        writeAll(all);
    }

    public void update(Appointment item) {
        List<Appointment> all = findAll();
        for (int i = 0; i < all.size(); i++)
            if (all.get(i).getId().equals(item.getId())) { all.set(i, item); writeAll(all); return; }
        throw new IllegalArgumentException("Appointment not found: " + item.getId());
    }

    public boolean delete(String id) {
        List<Appointment> all = findAll();
        boolean removed = all.removeIf(x -> x.getId().equals(id));
        if (removed) writeAll(all);
        return removed;
    }

    public String nextId() {
        List<String> ids = new ArrayList<>();
        for (Appointment x : findAll()) ids.add(x.getId());
        return FileStore.nextId("A", ids);
    }

    private void writeAll(List<Appointment> items) {
        List<String> lines = new ArrayList<>();
        for (Appointment x : items) lines.add(x.toFileString());
        FileStore.write(FILE, lines);
    }
}
