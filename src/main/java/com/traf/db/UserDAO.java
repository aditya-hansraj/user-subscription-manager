package com.traf.db;

import com.traf.core.User;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class UserDAO extends AbstractDAO<User> {
    public UserDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Optional<User> findById(long id) {
        return Optional.ofNullable(get(id));
    }

    public User create(User user) {
        return persist(user);
    }

    public List<User> findAll() {
        return list(namedTypedQuery("com.traf.core.User.findAll"));
    }

    public boolean deleteById(long id) {
        User user = get(id);
        if(user != null) {
            currentSession().remove(user);
            return true;
        }
        return false;
    }
}
