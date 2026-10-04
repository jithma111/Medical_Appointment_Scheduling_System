<%@ page contentType="text/html;charset=UTF-8" import="java.util.*" %>
<% request.setAttribute("pageTitle", "Admin Dashboard"); %>
<%@ include file="_header.jspf" %>
<%
    String tab = (String) request.getAttribute("tab");
    List<Doctor> doctors = (List<Doctor>) request.getAttribute("doctors");
    List<Patient> patients = (List<Patient>) request.getAttribute("patients");
    List<Appointment> appts = (List<Appointment>) request.getAttribute("appts");
    Map<String, String> names = (Map<String, String>) request.getAttribute("names");
    String[][] tabs = {{"doctors", "Doctors"}, {"patients", "Patients"}, {"appts", "All Appointments"}};
%>
<div class="row g-3 mb-4">
  <div class="col-md-4"><div class="card shadow-sm"><div class="card-body"><div class="text-muted">Doctors</div><div class="fs-3 fw-bold"><%= doctors.size() %></div></div></div></div>
  <div class="col-md-4"><div class="card shadow-sm"><div class="card-body"><div class="text-muted">Patients</div><div class="fs-3 fw-bold"><%= patients.size() %></div></div></div></div>
  <div class="col-md-4"><div class="card shadow-sm"><div class="card-body"><div class="text-muted">Appointments</div><div class="fs-3 fw-bold"><%= appts.size() %></div></div></div></div>
</div>
<ul class="nav nav-pills mb-4">
  <% for (String[] t : tabs) { %>
  <li class="nav-item"><a class="nav-link <%= t[0].equals(tab) ? "active" : "" %>" href="<%= ctx %>/admin?tab=<%= t[0] %>"><%= t[1] %></a></li>
  <% } %>
</ul>

<% if ("doctors".equals(tab)) { %>
  <p><button class="btn btn-brand" data-bs-toggle="collapse" data-bs-target="#addDoctor">+ Add doctor</button></p>
  <div class="collapse mb-4" id="addDoctor"><div class="card shadow-sm"><div class="card-body">
    <form method="post" action="<%= ctx %>/admin" class="row g-3">
      <input type="hidden" name="action" value="addDoctor">
      <div class="col-md-4"><label class="form-label">Name</label><input name="name" class="form-control" required></div>
      <div class="col-md-4"><label class="form-label">Specialization</label><input name="specialization" class="form-control" required></div>
      <div class="col-md-4"><label class="form-label">Consultation fee</label><input name="fee" type="number" min="0" step="0.01" class="form-control" required></div>
      <div class="col-md-4"><label class="form-label">Login username</label><input name="username" class="form-control" pattern="[A-Za-z0-9_]{4,20}" required></div>
      <div class="col-md-4"><label class="form-label">Login password</label><input name="password" type="password" minlength="6" class="form-control" required></div>
      <div class="col-md-4"><label class="form-label">Daily slots (HH:mm,...)</label><input name="slots" class="form-control" value="09:00,10:00,11:00" required></div>
      <div class="col-12"><button class="btn btn-brand">Save doctor</button></div>
    </form>
  </div></div></div>

  <div class="card shadow-sm"><div class="table-responsive">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-light"><tr><th>ID</th><th>Name</th><th>Specialization</th><th>Username</th><th>Slots</th><th>Fee</th><th></th></tr></thead>
      <tbody>
      <% if (doctors.isEmpty()) { %><tr><td colspan="7" class="text-center text-muted py-4">No doctors yet. Add the first one above.</td></tr><% } %>
      <% for (Doctor d : doctors) { %>
      <tr>
        <td><%= d.getId() %></td><td><%= Html.esc(d.getName()) %></td><td><%= Html.esc(d.getSpecialization()) %></td>
        <td><%= Html.esc(d.getUsername()) %></td><td><%= Html.esc(String.join(", ", d.getAvailableSlots())) %></td>
        <td>
          <form method="post" action="<%= ctx %>/admin" class="d-flex gap-1">
            <input type="hidden" name="action" value="updateFee"><input type="hidden" name="id" value="<%= d.getId() %>">
            <input name="fee" type="number" min="0" step="0.01" value="<%= d.getFee() %>" class="form-control form-control-sm" style="width:110px">
            <button class="btn btn-sm btn-outline-primary">Save</button>
          </form>
        </td>
        <td class="text-end">
          <form method="post" action="<%= ctx %>/admin" onsubmit="return confirm('Delete doctor <%= d.getId() %>?')">
            <input type="hidden" name="action" value="deleteDoctor"><input type="hidden" name="id" value="<%= d.getId() %>">
            <button class="btn btn-sm btn-outline-danger">Delete</button>
          </form>
        </td>
      </tr>
      <% } %>
      </tbody>
    </table>
  </div></div>

<% } else if ("patients".equals(tab)) { %>
  <div class="card shadow-sm"><div class="table-responsive">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-light"><tr><th>ID</th><th>Name</th><th>Age</th><th>Phone</th><th>Username</th><th></th></tr></thead>
      <tbody>
      <% if (patients.isEmpty()) { %><tr><td colspan="6" class="text-center text-muted py-4">No patients registered.</td></tr><% } %>
      <% for (Patient p : patients) { %>
      <tr><td><%= p.getId() %></td><td><%= Html.esc(p.getName()) %></td><td><%= p.getAge() %></td>
        <td><%= Html.esc(p.getPhone()) %></td><td><%= Html.esc(p.getUsername()) %></td>
        <td class="text-end">
          <form method="post" action="<%= ctx %>/admin" onsubmit="return confirm('Delete patient <%= p.getId() %>?')">
            <input type="hidden" name="action" value="deletePatient"><input type="hidden" name="id" value="<%= p.getId() %>">
            <button class="btn btn-sm btn-outline-danger">Delete</button>
          </form></td></tr>
      <% } %>
      </tbody>
    </table>
  </div></div>

<% } else if ("appts".equals(tab)) { %>
  <div class="card shadow-sm"><div class="table-responsive">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-light"><tr><th>ID</th><th>Date</th><th>Time</th><th>Patient</th><th>Doctor</th><th>Status</th></tr></thead>
      <tbody>
      <% if (appts.isEmpty()) { %><tr><td colspan="6" class="text-center text-muted py-4">No appointments yet.</td></tr><% } %>
      <% for (Appointment a : appts) { %>
      <tr><td><%= a.getId() %></td><td><%= a.getDate() %></td><td><%= a.getTime() %></td>
        <td><%= a.getPatientId() %> &ndash; <%= Html.esc(names.getOrDefault(a.getPatientId(), "deleted")) %></td>
        <td><%= a.getDoctorId() %> &ndash; <%= Html.esc(names.getOrDefault(a.getDoctorId(), "deleted")) %></td>
        <td><%= Html.badge(a.getStatus()) %></td></tr>
      <% } %>
      </tbody>
    </table>
  </div></div>
<% } %>
<%@ include file="_footer.jspf" %>
