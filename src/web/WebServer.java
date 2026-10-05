package web;

import com.sun.net.httpserver.*;
import model.*;
import service.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;

/**
 * Serves the CareSlot website (web/index.html) and a small JSON API that sits on top of the
 * existing services, so the website and the console app share the same data/ files.
 */
public class WebServer {
    private static final Object LOCK = new Object();            // one request at a time: files are read-modify-write
    private static final String COOKIE = "careslot_session";
    private static final String PW_CHARS = "abcdefghjkmnpqrstuvwxyz23456789";

    private final UserService users;
    private final PatientService patients;
    private final DoctorService doctors;
    private final AppointmentService appts;
    private final MedicalRecordService records;
    private final Path page;
    private final Map<String, String> sessions = new HashMap<>();   // token -> user id
    private final SecureRandom rnd = new SecureRandom();

    public WebServer(UserService u, PatientService p, DoctorService d, AppointmentService a,
                     MedicalRecordService r, Path page) {
        users = u; patients = p; doctors = d; appts = a; records = r; this.page = page;
    }

    public void start(String host, int port) throws IOException {
        HttpServer s = HttpServer.create(new InetSocketAddress(host, port), 0);
        s.createContext("/", this::handle);
        s.start();
        System.out.println("CareSlot is running at http://" + (host.equals("0.0.0.0") ? "localhost" : host) + ":" + port + "/");
        System.out.println("Press Ctrl+C to stop.");
    }

    // ---------------------------------------------------------------- routing
    private void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath(), method = ex.getRequestMethod();
        try {
            if (path.equals("/") || path.equals("/index.html")) {
                if (!method.equals("GET")) { send(ex, 405, "text/plain", "Method not allowed"); return; }
                send(ex, 200, "text/html; charset=utf-8", new String(Files.readAllBytes(page), StandardCharsets.UTF_8));
                return;
            }
            if (!path.startsWith("/api/")) { send(ex, 404, "text/plain", "Not found"); return; }
            Map<String, String> q = new HashMap<>(parse(ex.getRequestURI().getRawQuery()));
            if (method.equals("POST")) q.putAll(parse(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));
            Object out;
            synchronized (LOCK) { out = route(ex, method, path, q); }
            send(ex, 200, "application/json; charset=utf-8", Json.write(out));
        } catch (IllegalArgumentException e) {
            send(ex, 400, "application/json; charset=utf-8", Json.write(Json.obj("error", e.getMessage())));
        } catch (SecurityException e) {
            send(ex, 403, "application/json; charset=utf-8", Json.write(Json.obj("error", e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            send(ex, 500, "application/json; charset=utf-8", Json.write(Json.obj("error", "Server error")));
        }
    }

    private Object route(HttpExchange ex, String method, String path, Map<String, String> q) {
        User me = currentUser(ex);
        boolean post = method.equals("POST");
        switch (path) {
            case "/api/state": return state(me);
            case "/api/slots": return slots(q.get("doctor"), q.get("date"));
            case "/api/login": {
                need(post);
                User u = users.login(q.get("username"), q.get("password"));
                String token = newToken();
                sessions.put(token, u.getId());
                ex.getResponseHeaders().add("Set-Cookie", COOKIE + "=" + token + "; Path=/; HttpOnly; SameSite=Strict");
                return Json.obj("ok", true);
            }
            case "/api/logout": {
                need(post);
                String t = cookie(ex);
                if (t != null) sessions.remove(t);
                ex.getResponseHeaders().add("Set-Cookie", COOKIE + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Strict");
                return Json.obj("ok", true);
            }
            case "/api/register": {
                need(post);
                int age;
                try { age = Integer.parseInt(q.getOrDefault("age", "").trim()); }
                catch (NumberFormatException e) { throw new IllegalArgumentException("Age must be a number"); }
                patients.register(q.get("name"), age, q.getOrDefault("phone", "").trim(), q.get("username"), q.get("password"));
                User u = users.login(q.get("username"), q.get("password"));
                String token = newToken();
                sessions.put(token, u.getId());
                ex.getResponseHeaders().add("Set-Cookie", COOKIE + "=" + token + "; Path=/; HttpOnly; SameSite=Strict");
                return Json.obj("ok", true);
            }
            case "/api/book": {
                need(post);
                Patient p = asPatient(me);
                appts.book(p.getId(), q.get("doctor"), q.get("date"), q.get("time"), q.getOrDefault("reason", ""));
                return Json.obj("ok", true);
            }
            case "/api/cancel": {
                need(post);
                if (me == null) throw new SecurityException("Please log in first");
                String id = q.get("id");
                if (me instanceof Patient) appts.cancel(id, me.getId());
                else if (me instanceof Doctor) appts.cancelByDoctor(id, me.getId());
                else appts.cancelByAdmin(id);
                return Json.obj("ok", true);
            }
            case "/api/status": {
                need(post);
                if (!(me instanceof Doctor)) throw new SecurityException("Doctors only");
                String s = q.getOrDefault("status", "");
                if (s.equals("confirmed")) appts.confirm(q.get("id"), me.getId());
                else if (s.equals("cancelled")) appts.cancelByDoctor(q.get("id"), me.getId());
                else if (s.equals("completed")) {
                    String dx = q.getOrDefault("diagnosis", "").trim();
                    if (dx.isEmpty()) appts.complete(q.get("id"), me.getId());
                    else records.addRecord(me.getId(), q.get("id"), dx, q.getOrDefault("notes", ""), new ArrayList<Prescription>());
                } else throw new IllegalArgumentException("Unknown status");
                return Json.obj("ok", true);
            }
            case "/api/admin/doctor": {
                need(post); requireAdmin(me);
                String pw = randomPw();
                String uname = nextDoctorUsername();
                double fee;
                try { fee = Double.parseDouble(q.getOrDefault("fee", "0").trim()); }
                catch (NumberFormatException e) { throw new IllegalArgumentException("Fee must be a number"); }
                String slotsCsv = q.getOrDefault("slots", "").trim();
                if (slotsCsv.isEmpty()) slotsCsv = defaultSlots();
                Doctor d = doctors.addDoctor(q.get("name"), q.get("spec"), fee, uname, pw, slotsCsv);
                return Json.obj("name", d.getName(), "id", d.getUsername(), "pass", pw);
            }
            case "/api/admin/removeDoctor": need(post); requireAdmin(me); doctors.delete(q.get("id")); return Json.obj("ok", true);
            case "/api/admin/removePatient": need(post); requireAdmin(me); patients.delete(q.get("id")); return Json.obj("ok", true);
            case "/api/admin/reset": {
                need(post); requireAdmin(me);
                Doctor d = doctors.get(q.get("id"));
                String pw = randomPw();
                doctors.resetPassword(d.getId(), pw);
                return Json.obj("name", d.getName(), "id", d.getUsername(), "pass", pw);
            }
            default: throw new IllegalArgumentException("Unknown API call");
        }
    }

    // ---------------------------------------------------------------- views
    private Map<String, Object> state(User me) {
        List<Doctor> docs = doctors.findAll();
        List<Patient> pats = patients.findAll();
        List<Appointment> all = appts.findAll();

        Map<String, Object> s = new LinkedHashMap<>();
        s.put("me", me == null ? null : Json.obj("id", me.getId(), "name", me.getName(), "username", me.getUsername(),
                "role", me.getRole().toLowerCase(), "doc", me instanceof Doctor ? me.getId() : null));
        s.put("stats", Json.obj("doctors", docs.size(), "patients", pats.size(), "appts", all.size()));

        boolean admin = me instanceof Admin;
        List<Object> dl = new ArrayList<>();
        for (Doctor d : docs) {
            Map<String, Object> m = Json.obj("id", d.getId(), "name", d.getName(), "spec", d.getSpecialization(),
                    "fee", d.getFee(), "next", nextFree(d, all));
            if (admin) m.put("username", d.getUsername());
            dl.add(m);
        }
        s.put("doctors", dl);

        Map<String, String> pname = new HashMap<>();
        for (Patient p : pats) pname.put(p.getId(), p.getName());
        List<Object> al = new ArrayList<>();
        for (Appointment a : all) {
            boolean mine = me instanceof Patient && a.getPatientId().equals(me.getId())
                    || me instanceof Doctor && a.getDoctorId().equals(me.getId()) || admin;
            if (!mine) continue;
            al.add(Json.obj("id", a.getId(), "doc", a.getDoctorId(), "pid", a.getPatientId(),
                    "patient", pname.getOrDefault(a.getPatientId(), "Removed patient"), "date", a.getDate(),
                    "time", a.getTime(), "reason", a.getReason(), "status", a.getStatus().toLowerCase()));
        }
        s.put("appts", al);

        List<Object> pl = new ArrayList<>();
        if (admin) for (Patient p : pats) pl.add(Json.obj("id", p.getId(), "name", p.getName(), "username", p.getUsername()));
        s.put("patients", pl);

        List<Object> rl = new ArrayList<>();
        if (me instanceof Patient) {
            for (MedicalRecord r : records.historyForPatient(me.getId())) {
                String dn = doctors.findById(r.getDoctorId()).map(Doctor::getName).orElse("Removed doctor");
                rl.add(Json.obj("date", r.getDate(), "doctor", dn, "diagnosis", r.getDiagnosis(), "notes", r.getNotes()));
            }
        }
        s.put("records", rl);
        return s;
    }

    private Map<String, Object> slots(String doctorId, String date) {
        Doctor d = doctors.get(doctorId);
        Set<String> free = new HashSet<>(appts.availableSlots(doctorId, date));
        boolean today = date != null && date.trim().equals(LocalDate.now().toString());
        List<Object> out = new ArrayList<>();
        for (String t : d.getAvailableSlots()) {
            boolean past = today && LocalTime.parse(t).isBefore(LocalTime.now());
            out.add(Json.obj("t", t, "free", free.contains(t) && !past));
        }
        return Json.obj("slots", out);
    }

    private Map<String, Object> nextFree(Doctor d, List<Appointment> all) {
        for (int i = 0; i < 14; i++) {
            LocalDate day = LocalDate.now().plusDays(i);
            for (String t : d.getAvailableSlots()) {
                if (i == 0 && LocalTime.parse(t).isBefore(LocalTime.now())) continue;
                boolean taken = false;
                for (Appointment a : all)
                    if (a.getDoctorId().equals(d.getId()) && a.getDate().equals(day.toString())
                            && a.getTime().equals(t) && !Appointment.CANCELLED.equals(a.getStatus())) { taken = true; break; }
                if (!taken) return Json.obj("date", day.toString(), "time", t);
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- helpers
    private User currentUser(HttpExchange ex) {
        String t = cookie(ex), id = t == null ? null : sessions.get(t);
        return id == null ? null : users.findById(id);
    }

    private Patient asPatient(User u) {
        if (!(u instanceof Patient)) throw new SecurityException("Only patients can book");
        return (Patient) u;
    }

    private void requireAdmin(User u) { if (!(u instanceof Admin)) throw new SecurityException("Admin only"); }
    private void need(boolean post) { if (!post) throw new IllegalArgumentException("POST required"); }

    private String nextDoctorUsername() {
        int n = 1001;
        while (users.isUsernameTaken("DR" + n)) n++;
        return "DR" + n;
    }

    private String defaultSlots() {
        List<String> l = new ArrayList<>();
        for (int h = 9; h < 17; h++) { l.add(String.format("%02d:00", h)); l.add(String.format("%02d:30", h)); }
        return String.join(",", l);
    }

    private String randomPw() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) sb.append(PW_CHARS.charAt(rnd.nextInt(PW_CHARS.length())));
        return sb.toString();
    }

    private String newToken() { byte[] b = new byte[24]; rnd.nextBytes(b); return Base64.getUrlEncoder().withoutPadding().encodeToString(b); }

    private String cookie(HttpExchange ex) {
        List<String> hs = ex.getRequestHeaders().get("Cookie");
        if (hs == null) return null;
        for (String h : hs) for (String part : h.split(";")) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2 && kv[0].equals(COOKIE)) return kv[1];
        }
        return null;
    }

    private static Map<String, String> parse(String s) {
        Map<String, String> m = new HashMap<>();
        if (s == null || s.isEmpty()) return m;
        for (String pair : s.split("&")) {
            String[] kv = pair.split("=", 2);
            try {
                m.put(URLDecoder.decode(kv[0], "UTF-8"), kv.length > 1 ? URLDecoder.decode(kv[1], "UTF-8") : "");
            } catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
        }
        return m;
    }

    private static void send(HttpExchange ex, int code, String type, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type);
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }
}
