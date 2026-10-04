package ui;

import model.*;
import service.*;

public class LoginUI {
    private final UserService userService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService recordService;

    public LoginUI(UserService userService, PatientService patientService, DoctorService doctorService,
                   AppointmentService appointmentService, MedicalRecordService recordService) {
        this.userService = userService; this.patientService = patientService; this.doctorService = doctorService;
        this.appointmentService = appointmentService; this.recordService = recordService;
    }

    public void run() {
        System.out.println("=====================================");
        System.out.println(" Medical Appointment Scheduling System");
        System.out.println("=====================================");
        boolean running = true;
        while (running) {
            System.out.println("\n1. Login\n2. Register as a patient\n0. Exit");
            int choice = ConsoleInput.readInt("Choose: ", 0, 2);
            try {
                switch (choice) {
                    case 1: login(); break;
                    case 2: register(); break;
                    default: running = false;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Goodbye!");
    }

    private void login() {
        String username = ConsoleInput.text("Username: ");
        String password = ConsoleInput.text("Password: ");
        User user = userService.login(username, password);
        System.out.println("Welcome, " + user.getName() + " (" + user.getRole() + ")");
        if (user instanceof Patient)
            new PatientUI((Patient) user, patientService, doctorService, appointmentService, recordService).run();
        else if (user instanceof Doctor)
            new DoctorUI((Doctor) user, doctorService, patientService, appointmentService, recordService).run();
        else if (user instanceof Admin)
            new AdminUI((Admin) user, patientService, doctorService, appointmentService).run();
    }

    private void register() {
        String name = ConsoleInput.text("Full name: ");
        int age = ConsoleInput.readInt("Age: ", 0, 120);
        String phone = ConsoleInput.text("Phone (e.g. 0771234567): ");
        String username = ConsoleInput.text("Choose a username: ");
        String password = ConsoleInput.text("Choose a password (min 6 chars): ");
        Patient p = patientService.register(name, age, phone, username, password);
        System.out.println("Registered! Your patient ID is " + p.getId() + ". You can now log in.");
    }
}
