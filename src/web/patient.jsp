<%@ page contentType="text/html;charset=UTF-8" import="java.util.*, java.time.LocalDate" %>
<% request.setAttribute("pageTitle", "Patient Dashboard"); %>
<%@ include file="_header.jspf" %>
<%
    Patient me = (Patient) request.getAttribute("me");
    String tab = (String) request.getAttribute("tab");
    List<Appointment> appts = (List<Appointment>) request.getAttribute("appts");
    List<Doctor> doctors = (List<Doctor>) request.getAttribute("doctors");
    List<Doctor> allDoctors = (List<Doctor>) request.getAttribute("allDoctors");
    List<MedicalRecord> history = (List<MedicalRecord>) request.getAttribute("history");
    Map<String, String> docNames = (Map<String, String>) request.getAttribute("docNames");
    String q = (String) request.getAttribute("q");
    String bookDoctorId = (String) request.getAttribute("bookDoctorId");
    String bookDate = (String) request.getAttribute("bookDate");
    List<String> slots = (List<String>) request.getAttribute("slots");
    String slotError = (String) request.getAttribute("slotError");
    Appointment ridAppt = (Appointment) request.getAttribute("ridAppt");
    String rdate = (String) request.getAttribute("rdate");
    List<String> rslots = (List<String>) request.getAttribute("rslots");
    String today = LocalDate.now().toString();
    String[][] tabs = {{"appts", "My Appointments"}, {"book", "Book Appointment"}, {"doctors", "Find Doctors"},
                       {"history", "Medical History"}, {"profile", "My Profile"}};
%>
<h4 class="mb-3">Welcome, <%= Html.esc(me.getName()) %> <small class="text-muted">(<%= me.getId() %>)</small></h4>
<ul class="nav nav-pills mb-4 flex-wrap">
  <% for (String[] t : tabs) { %>
  <li class="nav-item"><a class="nav-link <%= t[0].equals(tab) ? "active" : "" %>" href="<%= ctx %>/patient?tab=<%= t[0] %>"><%= t[1] %></a></li>
  <% } %>
</ul>

<% if ("appts".equals(tab)) { %>
  <% if (ridAppt != null || slotError != null) { %>
  <div class="card shadow-sm mb-4"><div class="card-body">
    <% if (slotError != null) { %><div class="alert alert-danger"><%= Html.esc(slotError) %></div><% } %>
    <% if (ridAppt != null) { %>
    <h5>Reschedule appointment <%= ridAppt.getId() %></h5>
    <form method="get" action="<%= ctx %>/patient" class="row g-2 align-items-end mb-3">
      <input type="hidden" name="tab" value="appts"><input type="hidden" name="rid" value="<%= ridAppt.getId() %>">
      <div class="col-auto"><label class="form-label">New date</label>
        <input type="date" name="rdate" min="<%= today %>" value="<%= Html.esc(rdate) %>" class="form-control" required></div>
      <div class="col-auto"><button class="btn btn-outline-primary">Check free slots</button></div>
    </form>
    <% if (rslots != null) { if (rslots.isEmpty()) { %>
      <div class="alert alert-info mb-0">No free slots on that date.</div>
    <% } else { %>
      <form method="post" action="<%= ctx %>/patient">
        <input type="hidden" name="action" value="reschedule"><input type="hidden" name="id" value="<%= ridAppt.getId() %>">
        <input type="hidden" name="date" value="<%= Html.esc(rdate) %>">
        <div class="slot-pick d-flex flex-wrap gap-2 mb-3">
          <% int i = 0; for (String s : rslots) { i++; %>
          <input type="radio" class="btn-check" name="slot" id="rs<%= i %>" value="<%= s %>" required>
          <label class="btn btn-outline-primary" for="rs<%= i %>"><%= s %></label>
          <% } %>
        </div>
        <button class="btn btn-brand">Confirm new time</button>
        <a class="btn btn-link" href="<%= ctx %>/patient?tab=appts">Close</a>
      </form>
    <% } } } %>
  </div></div>
  <% } %>

  <div class="card shadow-sm"><div class="table-responsive">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-light"><tr><th>ID</th><th>Date</th><th>Time</th><th>Doctor</th><th>Status</th><th class="text-end">Actions</th></tr></thead>
      <tbody>
      <% if (appts.isEmpty()) { %><tr><td colspan="6" class="text-center text-muted py-4">No appointments yet. Use "Book Appointment" to make one.</td></tr><% } %>
      <% for (Appointment a : appts) { boolean active = Appointment.PENDING.equals(a.getStatus()) || Appointment.CONFIRMED.equals(a.getStatus()); %>
      <tr>
        <td><%= a.getId() %></td><td><%= a.getDate() %></td><td><%= a.getTime() %></td>
        <td><%= Html.esc(docNames.getOrDefault(a.getDoctorId(), a.getDoctorId())) %></td>
        <td><%= Html.badge(a.getStatus()) %></td>
        <td class="text-end">
          <% if (active) { %>
          <a class="btn btn-sm btn-outline-primary" href="<%= ctx %>/patient?tab=appts&rid=<%= a.getId() %>">Reschedule</a>
          <form method="post" action="<%= ctx %>/patient" class="d-inline" onsubmit="return confirm('Cancel appointment <%= a.getId() %>?')">
            <input type="hidden" name="action" value="cancel"><input type="hidden" name="id" value="<%= a.getId() %>">
            <button class="btn btn-sm btn-outline-danger">Cancel</button>
          </form>
          <% } %>
        </td>
      </tr>
      <% } %>
      </tbody>
    </table>
  </div></div>

<% } else if ("book".equals(tab)) { %>
  <div class="card shadow-sm"><div class="card-body">
    <h5 class="mb-3">Book an appointment</h5>
    <% if (allDoctors.isEmpty()) { %><div class="alert alert-info mb-0">No doctors are available yet. Please check back later.</div><% } else { %>
    <form method="get" action="<%= ctx %>/patient" class="row g-3 align-items-end mb-3">
      <input type="hidden" name="tab" value="book">
      <div class="col-md-5"><label class="form-label">Doctor</label>
        <select name="doctorId" class="form-select" required>
          <option value="">Choose a doctor...</option>
          <% for (Doctor d : allDoctors) { %>
          <option value="<%= d.getId() %>" <%= d.getId().equals(bookDoctorId) ? "selected" : "" %>>
            <%= Html.esc(d.getName()) %> - <%= Html.esc(d.getSpecialization()) %> (<%= Html.money(d.getFee()) %>)</option>
          <% } %>
        </select></div>
      <div class="col-md-3"><label class="form-label">Date</label>
        <input type="date" name="date" min="<%= today %>" value="<%= Html.esc(bookDate) %>" class="form-control" required></div>
      <div class="col-md-4"><button class="btn btn-outline-primary">Check available slots</button></div>
    </form>
    <% if (slotError != null) { %><div class="alert alert-danger"><%= Html.esc(slotError) %></div><% } %>
    <% if (slots != null) { if (slots.isEmpty()) { %>
      <div class="alert alert-info mb-0">No free slots for that doctor on that date. Try another day.</div>
    <% } else { %>
      <form method="post" action="<%= ctx %>/patient">
        <input type="hidden" name="action" value="book">
        <input type="hidden" name="doctorId" value="<%= Html.esc(bookDoctorId) %>">
        <input type="hidden" name="date" value="<%= Html.esc(bookDate) %>">
        <label class="form-label">Available time slots</label>
        <div class="slot-pick d-flex flex-wrap gap-2 mb-3">
          <% int i = 0; for (String s : slots) { i++; %>
          <input type="radio" class="btn-check" name="slot" id="bs<%= i %>" value="<%= s %>" required>
          <label class="btn btn-outline-primary" for="bs<%= i %>"><%= s %></label>
          <% } %>
        </div>
        <button class="btn btn-brand">Book appointment</button>
      </form>
    <% } } } %>
  </div></div>

<% } else if ("doctors".equals(tab)) { %>
  <form method="get" action="<%= ctx %>/patient" class="row g-2 mb-3">
    <input type="hidden" name="tab" value="doctors">
    <div class="col-md-6"><input name="q" value="<%= Html.esc(q) %>" class="form-control" placeholder="Search by doctor name or specialization"></div>
    <div class="col-auto"><button class="btn btn-brand">Search</button></div>
  </form>
  <div class="card shadow-sm"><div class="table-responsive">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-light"><tr><th>ID</th><th>Name</th><th>Specialization</th><th>Fee</th><th>Daily slots</th><th></th></tr></thead>
      <tbody>
      <% if (doctors.isEmpty()) { %><tr><td colspan="6" class="text-center text-muted py-4">No doctors found.</td></tr><% } %>
      <% for (Doctor d : doctors) { %>
      <tr><td><%= d.getId() %></td><td><%= Html.esc(d.getName()) %></td><td><%= Html.esc(d.getSpecialization()) %></td>
        <td><%= Html.money(d.getFee()) %></td><td><%= Html.esc(String.join(", ", d.getAvailableSlots())) %></td>
        <td class="text-end"><a class="btn btn-sm btn-brand" href="<%= ctx %>/patient?tab=book&doctorId=<%= d.getId() %>">Book</a></td></tr>
      <% } %>
      </tbody>
    </table>
  </div></div>

<% } else if ("history".equals(tab)) { %>
  <% if (history.isEmpty()) { %><div class="alert alert-info">No medical records yet. Records appear here after a doctor completes your visit.</div><% } %>
  <% for (MedicalRecord r : history) { %>
  <div class="card shadow-sm record-card mb-3"><div class="card-body">
    <div class="d-flex justify-content-between">
      <h6 class="mb-1"><%= Html.esc(r.getDiagnosis()) %></h6>
      <small class="text-muted"><%= r.getDate() %> &middot; <%= Html.esc(docNames.getOrDefault(r.getDoctorId(), r.getDoctorId())) %></small>
    </div>
    <% if (!r.getNotes().isEmpty()) { %><p class="mb-2 text-muted"><%= Html.esc(r.getNotes()) %></p><% } %>
    <% if (!r.getPrescriptions().isEmpty()) { %>
    <ul class="mb-0"><% for (Prescription p : r.getPrescriptions()) { %><li><strong><%= Html.esc(p.getMedicine()) %></strong> &ndash; <%= Html.esc(p.getDosage()) %>, <%= Html.esc(p.getDuration()) %></li><% } %></ul>
    <% } %>
  </div></div>
  <% } %>

<% } else if ("profile".equals(tab)) { %>
  <div class="card shadow-sm" style="max-width:560px"><div class="card-body">
    <h5 class="mb-3">My profile</h5>
    <form method="post" action="<%= ctx %>/patient">
      <input type="hidden" name="action" value="profile">
      <div class="row">
        <div class="col-8 mb-3"><label class="form-label">Name</label><input class="form-control" value="<%= Html.esc(me.getName()) %>" disabled></div>
        <div class="col-4 mb-3"><label class="form-label">Age</label><input class="form-control" value="<%= me.getAge() %>" disabled></div>
      </div>
      <div class="mb-3"><label class="form-label">Username</label><input class="form-control" value="<%= Html.esc(me.getUsername()) %>" disabled></div>
      <div class="mb-3"><label class="form-label">Phone</label>
        <input name="phone" class="form-control" value="<%= Html.esc(me.getPhone()) %>" pattern="0[0-9]{9}" required></div>
      <div class="mb-3"><label class="form-label">New password</label>
        <input name="password" type="password" minlength="6" class="form-control" placeholder="Leave blank to keep current password"></div>
      <button class="btn btn-brand">Save changes</button>
    </form>
  </div></div>
<% } %>
<%@ include file="_footer.jspf" %>
