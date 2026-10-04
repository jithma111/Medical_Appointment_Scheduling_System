package ui;

import model.*;
import service.*;

import java.util.*;

public class DoctorUI {
    private Doctor doctor;
    private final DoctorService doctors;
    private final PatientService patients;
    private final AppointmentService appointments;
    private final MedicalRecordService records;

    public DoctorUI(Doctor doctor, DoctorService doctors, PatientService patients,
                    AppointmentService appointments, MedicalRecordService records) {
        this.doctor = doctor; this.doctors = doctors; this.patients = patients;
        this.appointments = appointments; this.records = records;
    }

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Doctor Menu ---");
            System.out.println("1. My appointments\n2. Confirm an appointment\n3. Complete visit & add medical record");
            System.out.println("4. View a patient's medical history\n5. Update my daily time slots\n0. Logout");
            int choice = ConsoleInput.readInt("Choose: ", 0, 5);
            try {
                switch (choice) {
                    case 1: showAppointments(); break;
                    case 2: confirm(); break;
                    case 3: completeVisit(); break;
                    case 4: patientHistory(); break;
                    case 5: updateSlots(); break;
                    default: running = false;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void showAppointments() {
        List<Appointment> list = appointments.forDoctor(doctor.getId());
        if (list.isEmpty()) { System.out.println("You have no appointments."); return; }
        list.sort(Comparator.comparing(Appointment::getDate).thenComparing(Appointment::getTime));
        System.out.printf("%-6s %-12s %-6s %-8s %-20s %-10s%n", "ID", "Date", "Time", "Patient", "Name", "Status");
        for (Appointment a : list) {
            String name = patients.findById(a.getPatientId()).map(Patient::getName).orElse("?");
            System.out.printf("%-6s %-12s %-6s %-8s %-20s %-10s%n", a.getId(), a.getDate(), a.getTime(),
                    a.getPatientId(), name, a.getStatus());
        }
    }

    private void confirm() {
        showAppointments();
        String id = ConsoleInput.text("Appointment ID to confirm: ").toUpperCase();
        appointments.confirm(id, doctor.getId());
        System.out.println("Appointment confirmed.");
    }

    private void completeVisit() {
        showAppointments();
        String id = ConsoleInput.text("Appointment ID: ").toUpperCase();
        String diagnosis = ConsoleInput.text("Diagnosis: ");
        String notes = ConsoleInput.text("Notes (optional): ");
        List<Prescription> rx = new ArrayList<>();
        while (ConsoleInput.yesNo("Add a prescription?")) {
            String med = ConsoleInput.text("  Medicine: ");
            String dose = ConsoleInput.text("  Dosage: ");
            String dur = ConsoleInput.text("  Duration: ");
            rx.add(new Prescription(med, dose, dur));
        }
        MedicalRecord r = records.addRecord(doctor.getId(), id, diagnosis, notes, rx);
        System.out.println("Record " + r.getId() + " saved. Appointment marked COMPLETED.");
    }

    private void patientHistory() {
        String pid = ConsoleInput.text("Patient ID: ").toUpperCase();
        List<MedicalRecord> list = records.historyForDoctorView(doctor.getId(), pid);
        if (list.isEmpty()) { System.out.println("No records for this patient."); return; }
        for (MedicalRecord r : list) PatientUI.printRecord(r, doctors);
    }

    private void updateSlots() {
        System.out.println("Current slots: " + String.join(", ", doctor.getAvailableSlots()));
        String csv = ConsoleInput.text("New slots (comma-separated HH:mm): ");
        doctor = doctors.updateSlots(doctor.getId(), csv);
        System.out.println("Slots updated.");
    }
}
