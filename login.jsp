<%@ page contentType="text/html;charset=UTF-8" %>
<% request.setAttribute("pageTitle", "Login"); %>
<%@ include file="_header.jspf" %>
<% boolean reg = "register".equals(request.getAttribute("tab")); %>
<div class="auth-card">
  <div class="text-center mb-4">
    <h2 class="fw-bold">Medical Appointment Scheduling</h2>
    <p class="text-muted">Book and manage your doctor appointments online.</p>
  </div>
  <div class="card shadow-sm">
    <div class="card-body p-4">
      <ul class="nav nav-pills nav-fill mb-4">
        <li class="nav-item"><a class="nav-link <%= reg ? "" : "active" %>" href="<%= ctx %>/login">Login</a></li>
        <li class="nav-item"><a class="nav-link <%= reg ? "active" : "" %>" href="<%= ctx %>/login?tab=register">Register as Patient</a></li>
      </ul>

      <% if (!reg) { %>
      <form method="post" action="<%= ctx %>/login">
        <div class="mb-3"><label class="form-label">Username</label>
          <input name="username" class="form-control" required autofocus></div>
        <div class="mb-3"><label class="form-label">Password</label>
          <input name="password" type="password" class="form-control" required></div>
        <button class="btn btn-brand w-100">Login</button>
      </form>
      <% } else { %>
      <form method="post" action="<%= ctx %>/register">
        <div class="mb-3"><label class="form-label">Full name</label>
          <input name="name" class="form-control" required></div>
        <div class="row">
          <div class="col-4 mb-3"><label class="form-label">Age</label>
            <input name="age" type="number" min="0" max="120" class="form-control" required></div>
          <div class="col-8 mb-3"><label class="form-label">Phone</label>
            <input name="phone" class="form-control" placeholder="0771234567" pattern="0[0-9]{9}" required></div>
        </div>
        <div class="mb-3"><label class="form-label">Username</label>
          <input name="username" class="form-control" pattern="[A-Za-z0-9_]{4,20}" title="4-20 letters, digits or _" required></div>
        <div class="mb-3"><label class="form-label">Password</label>
          <input name="password" type="password" minlength="6" class="form-control" required>
          <div class="form-text">At least 6 characters.</div></div>
        <button class="btn btn-brand w-100">Create account</button>
      </form>
      <% } %>
    </div>
  </div>
</div>
<%@ include file="_footer.jspf" %>
