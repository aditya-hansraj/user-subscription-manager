package com.traf.service;

import com.traf.core.User;
import com.traf.db.UserDAO;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class UserService {

    private final UserDAO userDAO;

    @Inject
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Checks whether a user exists by id.
     */
    public boolean exists(long userId) {
        if (userId <= 0) return false;
        return userDAO.findById(userId).isPresent();
    }

    /**
     * Returns the user by id if present.
     */
    public Optional<User> getById(long userId) {
        if (userId <= 0) return Optional.empty();
        return userDAO.findById(userId);
    }
}
