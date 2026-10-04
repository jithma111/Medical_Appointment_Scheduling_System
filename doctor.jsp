<%@ page contentType="text/html;charset=UTF-8" import="java.util.*" %>
<% request.setAttribute("pageTitle", "Doctor Dashboard"); %>
<%@ include file="_header.jspf" %>
<%
    Doctor me = (Doctor) request.getAttribute("me");
    String tab = (String) request.getAttribute("tab");
    List<Appointment> appts = (List<Appointment>) request.getAttribute("appts");
    Map<String, String> patientNames = (Map<String, String>) request.getAttribute("patientNames");
    String patientId = (String) request.getAttribute("patientId");
    List<MedicalRecord> history = (List<MedicalRecord>) request.getAttribute("history");
    String historyError = (String) request.getAttribute("historyError");
    String[][] tabs = {{"appts", "Appointments"}, {"history", "Patient History"}, {"schedule", "My Schedule"}};
%>
<h4 class="mb-1">Dr. <%= Html.esc(me.getName()) %></h4>
<p class="text-muted"><%= Html.esc(me.getSpecialization()) %> &middot; Fee <%= Html.money(me.getFee()) %></p>
<ul class="nav nav-pills mb-4">
  <% for (String[] t : tabs) { %>
  <li class="nav-item"><a class="nav-link <%= t[0].equals(tab) ? "active" : "" %>" href="<%= ctx %>/doctor?tab=<%= t[0] %>"><%= t[1] %></a></li>
  <% } %>
</ul>

<% if ("appts".equals(tab)) { %>
  <div class="card shadow-sm"><div class="table-responsive">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-light"><tr><th>ID</th><th>Date</th><th>Time</th><th>Patient</th><th>Status</th><th class="text-end">Actions</th></tr></thead>
      <tbody>
      <% if (appts.isEmpty()) { %><tr><td colspan="6" class="text-center text-muted py-4">No appointments yet.</td></tr><% } %>
      <% for (Appointment a : appts) { %>
      <tr>
        <td><%= a.getId() %></td><td><%= a.getDate() %></td><td><%= a.getTime() %></td>
        <td><%= a.getPatientId() %> &ndash; <%= Html.esc(patientNames.getOrDefault(a.getPatientId(), "?")) %></td>
        <td><%= Html.badge(a.getStatus()) %></td>
        <td class="text-end">
          <% if (Appointment.PENDING.equals(a.getStatus())) { %>
          <form method="post" action="<%= ctx %>/doctor" class="d-inline">
            <input type="hidden" name="action" value="confirm"><input type="hidden" name="id" value="<%= a.getId() %>">
            <button class="btn btn-sm btn-brand">Confirm</button>
          </form>
          <% } else if (Appointment.CONFIRMED.equals(a.getStatus())) { %>
          <button type="button" class="btn btn-sm btn-success" data-complete="<%= a.getId() %>">Complete visit</button>
          <% } %>
        </td>
      </tr>
      <% } %>
      </tbody>
    </table>
  </div></div>

  <div class="modal fade" id="completeModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg">
      <form class="modal-content" method="post" action="<%= ctx %>/doctor">
        <input type="hidden" name="action" value="complete"><input type="hidden" name="id" id="completeId">
        <div class="modal-header"><h5 class="modal-title">Complete visit &ndash; <span id="completeTitle"></span></h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
        <div class="modal-body">
          <div class="mb-3"><label class="form-label">Diagnosis</label><input name="diagnosis" class="form-control" required></div>
          <div class="mb-3"><label class="form-label">Notes</label><textarea name="notes" rows="2" class="form-control"></textarea></div>
          <div class="d-flex justify-content-between align-items-center mb-2">
            <label class="form-label mb-0">Prescriptions</label>
            <button type="button" class="btn btn-sm btn-outline-primary" id="addRx">+ Add medicine</button>
          </div>
          <div id="rxRows"></div>
        </div>
        <div class="modal-footer"><button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button>
          <button class="btn btn-success">Save record &amp; complete</button></div>
      </form>
    </div>
  </div>
  <script>
    window.addEventListener('load', function () {
      var rows = document.getElementById('rxRows');
      function addRx() {
        var d = document.createElement('div');
        d.className = 'row g-2 mb-2';
        d.innerHTML = '<div class="col-5"><input name="medicine" class="form-control" placeholder="Medicine"></div>' +
          '<div class="col-3"><input name="dosage" class="form-control" placeholder="Dosage"></div>' +
          '<div class="col-3"><input name="duration" class="form-control" placeholder="Duration"></div>' +
          '<div class="col-1"><button type="button" class="btn btn-outline-danger w-100" onclick="this.closest(\'.row\').remove()">&times;</button></div>';
        rows.appendChild(d);
      }
      document.getElementById('addRx').addEventListener('click', addRx);
      var modal = new bootstrap.Modal(document.getElementById('completeModal'));
      document.querySelectorAll('[data-complete]').forEach(function (b) {
        b.addEventListener('click', function () {
          document.getElementById('completeId').value = b.dataset.complete;
          document.getElementById('completeTitle').textContent = b.dataset.complete;
          rows.innerHTML = ''; addRx();
          modal.show();
        });
      });
    });
  </script>

<% } else if ("history".equals(tab)) { %>
  <form method="get" action="<%= ctx %>/doctor" class="row g-2 mb-3">
    <input type="hidden" name="tab" value="history">
    <div class="col-md-4"><input name="patientId" value="<%= Html.esc(patientId) %>" class="form-control" placeholder="Patient ID, e.g. P001" required></div>
    <div class="col-auto"><button class="btn btn-brand">View history</button></div>
  </form>
  <% if (historyError != null) { %><div class="alert alert-danger"><%= Html.esc(historyError) %></div><% } %>
  <% if (history != null && history.isEmpty()) { %><div class="alert alert-info">No medical records for this patient yet.</div><% } %>
  <% if (history != null) for (MedicalRecord r : history) { %>
  <div class="card shadow-sm record-card mb-3"><div class="card-body">
    <div class="d-flex justify-content-between"><h6 class="mb-1"><%= Html.esc(r.getDiagnosis()) %></h6><small class="text-muted"><%= r.getDate() %> &middot; <%= r.getId() %></small></div>
    <% if (!r.getNotes().isEmpty()) { %><p class="mb-2 text-muted"><%= Html.esc(r.getNotes()) %></p><% } %>
    <% if (!r.getPrescriptions().isEmpty()) { %><ul class="mb-0"><% for (Prescription p : r.getPrescriptions()) { %><li><strong><%= Html.esc(p.getMedicine()) %></strong> &ndash; <%= Html.esc(p.getDosage()) %>, <%= Html.esc(p.getDuration()) %></li><% } %></ul><% } %>
  </div></div>
  <% } %>

<% } else if ("schedule".equals(tab)) { %>
  <div class="card shadow-sm" style="max-width:620px"><div class="card-body">
    <h5 class="mb-3">Daily time slots</h5>
    <form method="post" action="<%= ctx %>/doctor">
      <input type="hidden" name="action" value="slots">
      <div class="mb-3"><label class="form-label">Slots (24-hour HH:mm, comma-separated)</label>
        <input name="slots" class="form-control" value="<%= Html.esc(String.join(",", me.getAvailableSlots())) %>" required>
        <div class="form-text">Example: 09:00,09:30,10:00,14:00</div></div>
      <button class="btn btn-brand">Save slots</button>
    </form>
  </div></div>
<% } %>
<%@ include file="_footer.jspf" %>
