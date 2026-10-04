import repository.*;
import service.*;
import ui.LoginUI;

public class Main {
    public static void main(String[] args) {
        // repositories
        PatientRepository patientRepo = new PatientRepository();
        DoctorRepository doctorRepo = new DoctorRepository();
        AppointmentRepository appointmentRepo = new AppointmentRepository();
        UserRepository userRepo = new UserRepository(patientRepo, doctorRepo);

        // services
        UserService userService = new UserService(userRepo);
        PatientService patientService = new PatientService(patientRepo, userRepo);
        DoctorService doctorService = new DoctorService(doctorRepo, userRepo);
        AppointmentService appointmentService = new AppointmentService(appointmentRepo, doctorRepo, patientRepo);
        MedicalRecordService recordService = new MedicalRecordService(appointmentService);

        // UI
        try {
            new LoginUI(userService, patientService, doctorService, appointmentService, recordService).run();
        } catch (IllegalStateException e) {
            System.out.println("\nInput closed. Exiting.");
        }
    }
}
