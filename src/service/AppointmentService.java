package service;

import model.*;
import repository.*;

import java.time.LocalDate;
import java.util.*;

public class AppointmentService {
    private final AppointmentRepository appointments;
    private final DoctorRepository doctors;
    private final PatientRepository patients;

    public AppointmentService(AppointmentRepository appointments, DoctorRepository doctors, PatientRepository patients) {
        this.appointments = appointments; this.doctors = doctors; this.patients = patients;
    }

    /** The doctor's daily slots minus the ones already booked (cancelled bookings free the slot). */
    public List<String> availableSlots(String doctorId, String date) {
        Doctor d = doctors.findById(doctorId).orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        String day = parseFutureDate(date).toString();
        Set<String> taken = new HashSet<>();
        for (Appointment a : appointments.findAll())
            if (a.getDoctorId().equals(doctorId) && a.getDate().equals(day) && !Appointment.CANCELLED.equals(a.getStatus()))
                taken.add(a.getTime());
        List<String> free = new ArrayList<>();
        for (String s : d.getAvailableSlots()) if (!taken.contains(s)) free.add(s);
        return free;
    }

    public Appointment book(String patientId, String doctorId, String date, String time) {
        patients.findById(patientId).orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        String day = parseFutureDate(date).toString();
        if (!availableSlots(doctorId, day).contains(time)) throw new IllegalArgumentException("That slot is not available");
        Appointment a = new Appointment(appointments.nextId(), patientId, doctorId, day, time, Appointment.PENDING);
        appointments.save(a);
        return a;
    }

    public Appointment reschedule(String appointmentId, String patientId, String newDate, String newTime) {
        Appointment a = get(appointmentId);
        requireOwner(a, patientId);
        requireActive(a);
        String day = parseFutureDate(newDate).toString();
        if (!availableSlots(a.getDoctorId(), day).contains(newTime)) throw new IllegalArgumentException("That slot is not available");
        a.setDate(day); a.setTime(newTime); a.setStatus(Appointment.PENDING);
        appointments.update(a);
        return a;
    }

    public void cancel(String appointmentId, String patientId) {
        Appointment a = get(appointmentId);
        requireOwner(a, patientId);
        requireActive(a);
        a.setStatus(Appointment.CANCELLED);
        appointments.update(a);
    }

    public void confirm(String appointmentId, String doctorId) {
        Appointment a = getForDoctor(appointmentId, doctorId);
        if (!Appointment.PENDING.equals(a.getStatus())) throw new IllegalArgumentException("Only pending appointments can be confirmed");
        a.setStatus(Appointment.CONFIRMED);
        appointments.update(a);
    }

    public void complete(String appointmentId, String doctorId) {
        Appointment a = getForDoctor(appointmentId, doctorId);
        if (!Appointment.CONFIRMED.equals(a.getStatus())) throw new IllegalArgumentException("Only confirmed appointments can be completed");
        a.setStatus(Appointment.COMPLETED);
        appointments.update(a);
    }

    public Appointment get(String id) {
        return appointments.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
    }

    public Appointment getForDoctor(String appointmentId, String doctorId) {
        Appointment a = get(appointmentId);
        if (!a.getDoctorId().equals(doctorId)) throw new IllegalArgumentException("This appointment belongs to another doctor");
        return a;
    }

    public List<Appointment> forPatient(String patientId) {
        List<Appointment> out = new ArrayList<>();
        for (Appointment a : appointments.findAll()) if (a.getPatientId().equals(patientId)) out.add(a);
        return out;
    }

    public List<Appointment> forDoctor(String doctorId) {
        List<Appointment> out = new ArrayList<>();
        for (Appointment a : appointments.findAll()) if (a.getDoctorId().equals(doctorId)) out.add(a);
        return out;
    }

    public List<Appointment> findAll() { return appointments.findAll(); }

    private void requireOwner(Appointment a, String patientId) {
        if (!a.getPatientId().equals(patientId)) throw new IllegalArgumentException("This appointment belongs to another patient");
    }

    private void requireActive(Appointment a) {
        if (Appointment.COMPLETED.equals(a.getStatus()) || Appointment.CANCELLED.equals(a.getStatus()))
            throw new IllegalArgumentException("A " + a.getStatus().toLowerCase() + " appointment cannot be changed");
    }

    private LocalDate parseFutureDate(String date) {
        LocalDate d;
        try { d = LocalDate.parse(date == null ? "" : date.trim()); }
        catch (Exception e) { throw new IllegalArgumentException("Date must be in yyyy-MM-dd format"); }
        if (d.isBefore(LocalDate.now())) throw new IllegalArgumentException("Date cannot be in the past");
        return d;
    }
}
