package com.traf.db;

import com.traf.core.Plan;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class PlanDAO extends AbstractDAO<Plan> {
    public PlanDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Optional<Plan> findById(long id) {
        return Optional.ofNullable(get(id));
    }

    public Plan create(Plan plan) {
        return persist(plan);
    }

    public List<Plan> findAll() {
        return list(namedTypedQuery("com.traf.core.Plan.findAll"));
    }

    public boolean deleteById(long id) {
        Plan plan = get(id);
        if (plan != null) {
            currentSession().remove(plan);
            return true;
        }
        return false;
    }
}
