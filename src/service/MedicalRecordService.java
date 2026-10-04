package service;

import model.*;
import repository.FileStore;

import java.time.LocalDate;
import java.util.*;

public class MedicalRecordService {
    private static final String FILE = "medical_records.txt";
    private final AppointmentService appointments;

    public MedicalRecordService(AppointmentService appointments) { this.appointments = appointments; }

    /** Doctor records the visit; the appointment is then marked COMPLETED. */
    public MedicalRecord addRecord(String doctorId, String appointmentId, String diagnosis, String notes,
                                   List<Prescription> prescriptions) {
        Appointment a = appointments.getForDoctor(appointmentId, doctorId);
        if (!Appointment.CONFIRMED.equals(a.getStatus()))
            throw new IllegalArgumentException("A record can only be added for a confirmed appointment");
        diagnosis = FileStore.clean(diagnosis);
        if (diagnosis.isEmpty()) throw new IllegalArgumentException("Diagnosis is required");

        List<Prescription> cleaned = new ArrayList<>();
        for (Prescription p : prescriptions)
            cleaned.add(new Prescription(FileStore.clean(p.getMedicine()), FileStore.clean(p.getDosage()), FileStore.clean(p.getDuration())));

        List<MedicalRecord> all = findAll();
        List<String> ids = new ArrayList<>();
        for (MedicalRecord r : all) ids.add(r.getId());
        MedicalRecord rec = new MedicalRecord(FileStore.nextId("M", ids), a.getPatientId(), doctorId, appointmentId,
                LocalDate.now().toString(), diagnosis, FileStore.clean(notes), cleaned);
        all.add(rec);
        writeAll(all);
        appointments.complete(appointmentId, doctorId);
        return rec;
    }

    public List<MedicalRecord> historyForPatient(String patientId) {
        List<MedicalRecord> out = new ArrayList<>();
        for (MedicalRecord r : findAll()) if (r.getPatientId().equals(patientId)) out.add(r);
        return out;
    }

    /** A doctor may only view the history of patients who have booked with them. */
    public List<MedicalRecord> historyForDoctorView(String doctorId, String patientId) {
        boolean hasVisit = false;
        for (Appointment a : appointments.forDoctor(doctorId)) if (a.getPatientId().equals(patientId)) hasVisit = true;
        if (!hasVisit) throw new IllegalArgumentException("This patient has no appointments with you");
        return historyForPatient(patientId);
    }

    private List<MedicalRecord> findAll() {
        List<MedicalRecord> list = new ArrayList<>();
        for (String line : FileStore.read(FILE)) list.add(MedicalRecord.fromFileString(line));
        return list;
    }

    private void writeAll(List<MedicalRecord> records) {
        List<String> lines = new ArrayList<>();
        for (MedicalRecord r : records) lines.add(r.toFileString());
        FileStore.write(FILE, lines);
    }
}
