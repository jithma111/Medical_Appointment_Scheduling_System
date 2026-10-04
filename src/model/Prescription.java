package model;

public class Prescription {
    private final String medicine;
    private final String dosage;
    private final String duration;

    public Prescription(String medicine, String dosage, String duration) {
        this.medicine = medicine; this.dosage = dosage; this.duration = duration;
    }

    public String getMedicine() { return medicine; }
    public String getDosage() { return dosage; }
    public String getDuration() { return duration; }

    // medicine~dosage~duration
    public String toFileString() { return medicine + "~" + dosage + "~" + duration; }

    public static Prescription fromFileString(String s) {
        String[] f = s.split("~", -1);
        return new Prescription(f[0], f[1], f[2]);
    }

    @Override public String toString() { return medicine + " - " + dosage + " for " + duration; }
}
