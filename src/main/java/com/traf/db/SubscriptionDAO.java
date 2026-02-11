package com.traf.db;

import com.traf.core.Subscription;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class SubscriptionDAO extends AbstractDAO<Subscription> {

    public SubscriptionDAO(SessionFactory factory) {
        super(factory);
    }

    public Optional<Subscription> findById(Long id) {
        return Optional.ofNullable(get(id));
    }

    public Subscription create(Subscription subscription) {
        return persist(subscription);
    }

    public List<Subscription> findByUserId(long userId) {
        return list(query("FROM Subscription s WHERE s.user.id = :userId")
                .setParameter("userId", userId));
    }

    public void delete(Subscription subscription) {
        currentSession().remove(subscription);
    }

    public int getCurrentUsage(long userId) {
        // Use a typed HQL query to return a Number directly and avoid casting
        String sql = "SELECT s.currentUsage FROM Subscription s WHERE s.user.id = :userId";
        Query<Number> query = currentSession().createQuery(sql, Number.class);
        query.setParameter("userId", userId);

        Number result = query.uniqueResult();
        if (result == null) {
            return 0; // no usage found
        }
        return result.intValue();
    }

    public int getPlanLimit(long userId) {
        String sql = "SELECT s.plan.maxCredits FROM Subscription s WHERE s.user.id = :userId AND s.status = 'ACTIVE'";
        Query<Number> query = currentSession().createQuery(sql, Number.class);
        query.setParameter("userId", userId);

        Number result = query.uniqueResult();
        if (result == null) {
            return 0;
        }
        return result.intValue();
    }

    public void updateUsage(long userId, int usage) {
        String sql = "UPDATE Subscription s SET s.currentUsage = :usage WHERE s.user.id = :userId";

        // Use the mutation query API to express a bulk update clearly and avoid deprecated createQuery(String)
        org.hibernate.query.MutationQuery mutation = currentSession().createMutationQuery(sql);
        mutation.setParameter("usage", usage);
        mutation.setParameter("userId", userId);
        mutation.executeUpdate();
    }
}