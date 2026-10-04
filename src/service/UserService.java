package service;

import model.User;
import repository.UserRepository;

public class UserService {
    private final UserRepository users;

    public UserService(UserRepository users) { this.users = users; }

    public User login(String username, String password) {
        User u = users.findByUsername(username == null ? "" : username.trim()).orElse(null);
        if (u == null || !u.checkPassword(password == null ? "" : password))
            throw new IllegalArgumentException("Invalid username or password");
        return u;
    }

    public boolean isUsernameTaken(String username) { return users.usernameExists(username); }
}
