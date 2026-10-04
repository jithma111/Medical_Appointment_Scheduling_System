package ui;

import model.*;
import service.*;

import java.util.List;

public class AdminUI {
    private final Admin admin;
    private final PatientService patients;
    private final DoctorService doctors;
    private final AppointmentService appointments;

    public AdminUI(Admin admin, PatientService patients, DoctorService doctors, AppointmentService appointments) {
        this.admin = admin; this.patients = patients; this.doctors = doctors; this.appointments = appointments;
    }

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Admin Menu (" + admin.getName() + ") ---");
            System.out.println("1. Add doctor\n2. List doctors\n3. Update doctor fee\n4. Delete doctor");
            System.out.println("5. List patients\n6. Delete patient\n7. All appointments\n0. Logout");
            int choice = ConsoleInput.readInt("Choose: ", 0, 7);
            try {
                switch (choice) {
                    case 1: addDoctor(); break;
                    case 2: PatientUI.printDoctors(doctors.findAll()); break;
                    case 3: updateFee(); break;
                    case 4: deleteDoctor(); break;
                    case 5: listPatients(); break;
                    case 6: deletePatient(); break;
                    case 7: allAppointments(); break;
                    default: running = false;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void addDoctor() {
        String name = ConsoleInput.text("Doctor name: ");
        String spec = ConsoleInput.text("Specialization: ");
        double fee = ConsoleInput.readDouble("Consultation fee: ", 0);
        String username = ConsoleInput.text("Login username: ");
        String password = ConsoleInput.text("Login password (min 6 chars): ");
        String slots = ConsoleInput.text("Daily slots (comma-separated HH:mm, e.g. 09:00,10:00,11:00): ");
        Doctor d = doctors.addDoctor(name, spec, fee, username, password, slots);
        System.out.println("Doctor added with ID " + d.getId());
    }

    private void updateFee() {
        PatientUI.printDoctors(doctors.findAll());
        String id = ConsoleInput.text("Doctor ID: ").toUpperCase();
        double fee = ConsoleInput.readDouble("New fee: ", 0);
        doctors.updateFee(id, fee);
        System.out.println("Fee updated.");
    }

    private void deleteDoctor() {
        PatientUI.printDoctors(doctors.findAll());
        String id = ConsoleInput.text("Doctor ID to delete: ").toUpperCase();
        if (ConsoleInput.yesNo("Delete " + id + "?")) { doctors.delete(id); System.out.println("Doctor deleted."); }
    }

    private void listPatients() {
        List<Patient> list = patients.findAll();
        if (list.isEmpty()) { System.out.println("No patients registered."); return; }
        System.out.printf("%-6s %-20s %-5s %-12s %s%n", "ID", "Name", "Age", "Phone", "Username");
        for (Patient p : list)
            System.out.printf("%-6s %-20s %-5d %-12s %s%n", p.getId(), p.getName(), p.getAge(), p.getPhone(), p.getUsername());
    }

    private void deletePatient() {
        listPatients();
        String id = ConsoleInput.text("Patient ID to delete: ").toUpperCase();
        if (ConsoleInput.yesNo("Delete " + id + "?")) { patients.delete(id); System.out.println("Patient deleted."); }
    }

    private void allAppointments() {
        List<Appointment> list = appointments.findAll();
        if (list.isEmpty()) { System.out.println("No appointments."); return; }
        System.out.printf("%-6s %-12s %-6s %-8s %-8s %-10s%n", "ID", "Date", "Time", "Patient", "Doctor", "Status");
        for (Appointment a : list)
            System.out.printf("%-6s %-12s %-6s %-8s %-8s %-10s%n", a.getId(), a.getDate(), a.getTime(),
                    a.getPatientId(), a.getDoctorId(), a.getStatus());
    }
}
