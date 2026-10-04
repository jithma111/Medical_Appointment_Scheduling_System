package model;

import java.util.*;

public class MedicalRecord {
    private final String id;
    private final String patientId;
    private final String doctorId;
    private final String appointmentId;
    private final String date;
    private final String diagnosis;
    private final String notes;
    private final List<Prescription> prescriptions;

    public MedicalRecord(String id, String patientId, String doctorId, String appointmentId, String date,
                         String diagnosis, String notes, List<Prescription> prescriptions) {
        this.id = id; this.patientId = patientId; this.doctorId = doctorId; this.appointmentId = appointmentId;
        this.date = date; this.diagnosis = diagnosis; this.notes = notes;
        this.prescriptions = new ArrayList<>(prescriptions);
    }

    public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public String getAppointmentId() { return appointmentId; }
    public String getDate() { return date; }
    public String getDiagnosis() { return diagnosis; }
    public String getNotes() { return notes; }
    public List<Prescription> getPrescriptions() { return new ArrayList<>(prescriptions); }

    // id|patientId|doctorId|appointmentId|date|diagnosis|notes|presc1;presc2
    public String toFileString() {
        List<String> ps = new ArrayList<>();
        for (Prescription p : prescriptions) ps.add(p.toFileString());
        return String.join("|", id, patientId, doctorId, appointmentId, date, diagnosis, notes, String.join(";", ps));
    }

    public static MedicalRecord fromFileString(String line) {
        String[] f = line.split("\\|", -1);
        List<Prescription> ps = new ArrayList<>();
        if (!f[7].isEmpty()) for (String s : f[7].split(";")) ps.add(Prescription.fromFileString(s));
        return new MedicalRecord(f[0], f[1], f[2], f[3], f[4], f[5], f[6], ps);
    }
}
