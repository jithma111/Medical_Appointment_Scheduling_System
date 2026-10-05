import repository.*;
import service.*;
import web.WebServer;

import java.nio.file.Path;
import java.nio.file.Paths;

/** Starts the CareSlot website on top of the same services and data/ files the console app uses.
 *  Usage: java -cp out WebMain [port] [host]     (defaults: 8080, 127.0.0.1) */
public class WebMain {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        String host = args.length > 1 ? args[1] : "127.0.0.1";

        PatientRepository patientRepo = new PatientRepository();
        DoctorRepository doctorRepo = new DoctorRepository();
        AppointmentRepository appointmentRepo = new AppointmentRepository();
        UserRepository userRepo = new UserRepository(patientRepo, doctorRepo);

        UserService userService = new UserService(userRepo);
        PatientService patientService = new PatientService(patientRepo, userRepo);
        DoctorService doctorService = new DoctorService(doctorRepo, userRepo);
        AppointmentService appointmentService = new AppointmentService(appointmentRepo, doctorRepo, patientRepo);
        MedicalRecordService recordService = new MedicalRecordService(appointmentService);

        if (doctorService.findAll().isEmpty()) seedDemoDoctors(doctorService);

        Path page = Paths.get("web", "index.html");
        if (!page.toFile().exists()) { System.err.println("Cannot find " + page.toAbsolutePath() + " - run from the project folder."); return; }
        new WebServer(userService, patientService, doctorService, appointmentService, recordService, page).start(host, port);
    }

    private static void seedDemoDoctors(DoctorService ds) {
        String slots = "09:00,09:30,10:00,10:30,11:00,11:30,12:00,12:30,13:00,13:30,14:00,14:30,15:00,15:30,16:00,16:30";
        ds.addDoctor("Dr. Amara Perera", "General Practice", 3000, "DR1001", "demo123", slots);
        ds.addDoctor("Dr. Kavi Silva", "Cardiology", 5000, "DR1002", "demo123", slots);
        ds.addDoctor("Dr. Nadia Rahman", "Pediatrics", 4000, "DR1003", "demo123", slots);
        System.out.println("First run: added 3 demo doctors (DR1001-DR1003, password demo123). Change or remove them in the admin page.");
    }
}
