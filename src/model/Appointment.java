package model;

public class Appointment {
    public static final String PENDING = "PENDING";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";

    private final String id;
    private final String patientId;
    private final String doctorId;
    private String date;     // yyyy-MM-dd
    private String time;     // HH:mm
    private String status;

    public Appointment(String id, String patientId, String doctorId, String date, String time, String status) {
        this.id = id; this.patientId = patientId; this.doctorId = doctorId;
        this.date = date; this.time = time; this.status = status;
    }

    public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // id|patientId|doctorId|date|time|status
    public String toFileString() { return String.join("|", id, patientId, doctorId, date, time, status); }

    public static Appointment fromFileString(String line) {
        String[] f = line.split("\\|", -1);
        return new Appointment(f[0], f[1], f[2], f[3], f[4], f[5]);
    }
}
