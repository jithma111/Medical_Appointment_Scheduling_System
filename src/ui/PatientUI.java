package ui;

import model.*;
import service.*;

import java.util.List;

public class PatientUI {
    private Patient patient;
    private final PatientService patients;
    private final DoctorService doctors;
    private final AppointmentService appointments;
    private final MedicalRecordService records;

    public PatientUI(Patient patient, PatientService patients, DoctorService doctors,
                     AppointmentService appointments, MedicalRecordService records) {
        this.patient = patient; this.patients = patients; this.doctors = doctors;
        this.appointments = appointments; this.records = records;
    }

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Patient Menu ---");
            System.out.println("1. View profile\n2. Update profile\n3. Find doctors\n4. Book appointment");
            System.out.println("5. My appointments\n6. Reschedule appointment\n7. Cancel appointment");
            System.out.println("8. My medical history\n0. Logout");
            int choice = ConsoleInput.readInt("Choose: ", 0, 8);
            try {
                switch (choice) {
                    case 1: viewProfile(); break;
                    case 2: updateProfile(); break;
                    case 3: findDoctors(); break;
                    case 4: book(); break;
                    case 5: showAppointments(); break;
                    case 6: reschedule(); break;
                    case 7: cancel(); break;
                    case 8: history(); break;
                    default: running = false;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void viewProfile() {
        System.out.println("ID: " + patient.getId() + " | Name: " + patient.getName() + " | Age: " + patient.getAge()
                + " | Phone: " + patient.getPhone() + " | Username: " + patient.getUsername());
    }

    private void updateProfile() {
        String phone = ConsoleInput.text("New phone [" + patient.getPhone() + "]: ");
        if (phone.isEmpty()) phone = patient.getPhone();
        String pw = ConsoleInput.text("New password (leave blank to keep): ");
        patient = patients.updateProfile(patient.getId(), phone, pw);
        System.out.println("Profile updated.");
    }

    private void findDoctors() {
        String k = ConsoleInput.text("Search by name or specialization (blank = all): ");
        printDoctors(doctors.search(k));
    }

    private void book() {
        printDoctors(doctors.findAll());
        String doctorId = ConsoleInput.text("Doctor ID: ").toUpperCase();
        String date = ConsoleInput.text("Date (yyyy-MM-dd): ");
        List<String> slots = appointments.availableSlots(doctorId, date);
        if (slots.isEmpty()) { System.out.println("No free slots on that date."); return; }
        for (int i = 0; i < slots.size(); i++) System.out.println((i + 1) + ". " + slots.get(i));
        int pick = ConsoleInput.readInt("Pick a slot: ", 1, slots.size());
        Appointment a = appointments.book(patient.getId(), doctorId, date, slots.get(pick - 1));
        System.out.println("Booked! Appointment " + a.getId() + " on " + a.getDate() + " at " + a.getTime()
                + " (status: " + a.getStatus() + ").");
    }

    private void showAppointments() {
        List<Appointment> list = appointments.forPatient(patient.getId());
        if (list.isEmpty()) { System.out.println("You have no appointments."); return; }
        System.out.printf("%-6s %-12s %-6s %-20s %-10s%n", "ID", "Date", "Time", "Doctor", "Status");
        for (Appointment a : list) {
            String doc = doctors.findById(a.getDoctorId()).map(Doctor::getName).orElse(a.getDoctorId());
            System.out.printf("%-6s %-12s %-6s %-20s %-10s%n", a.getId(), a.getDate(), a.getTime(), doc, a.getStatus());
        }
    }

    private void reschedule() {
        showAppointments();
        String id = ConsoleInput.text("Appointment ID to reschedule: ").toUpperCase();
        Appointment current = appointments.get(id);
        String date = ConsoleInput.text("New date (yyyy-MM-dd): ");
        List<String> slots = appointments.availableSlots(current.getDoctorId(), date);
        if (slots.isEmpty()) { System.out.println("No free slots on that date."); return; }
        for (int i = 0; i < slots.size(); i++) System.out.println((i + 1) + ". " + slots.get(i));
        int pick = ConsoleInput.readInt("Pick a slot: ", 1, slots.size());
        Appointment a = appointments.reschedule(id, patient.getId(), date, slots.get(pick - 1));
        System.out.println("Rescheduled to " + a.getDate() + " at " + a.getTime() + " (awaiting confirmation).");
    }

    private void cancel() {
        showAppointments();
        String id = ConsoleInput.text("Appointment ID to cancel: ").toUpperCase();
        if (ConsoleInput.yesNo("Cancel " + id + "?")) {
            appointments.cancel(id, patient.getId());
            System.out.println("Appointment cancelled.");
        }
    }

    private void history() {
        List<MedicalRecord> list = records.historyForPatient(patient.getId());
        if (list.isEmpty()) { System.out.println("No medical records yet."); return; }
        for (MedicalRecord r : list) printRecord(r, doctors);
    }

    static void printDoctors(List<Doctor> list) {
        if (list.isEmpty()) { System.out.println("No doctors found."); return; }
        System.out.printf("%-6s %-20s %-18s %-10s %s%n", "ID", "Name", "Specialization", "Fee", "Daily slots");
        for (Doctor d : list)
            System.out.printf("%-6s %-20s %-18s %-10.2f %s%n", d.getId(), d.getName(), d.getSpecialization(),
                    d.getFee(), String.join(", ", d.getAvailableSlots()));
    }

    static void printRecord(MedicalRecord r, DoctorService doctors) {
        String doc = doctors.findById(r.getDoctorId()).map(Doctor::getName).orElse(r.getDoctorId());
        System.out.println("\n[" + r.getId() + "] " + r.getDate() + " with " + doc + " (appointment " + r.getAppointmentId() + ")");
        System.out.println("  Diagnosis: " + r.getDiagnosis());
        if (!r.getNotes().isEmpty()) System.out.println("  Notes: " + r.getNotes());
        for (Prescription p : r.getPrescriptions()) System.out.println("  Rx: " + p);
    }
}
