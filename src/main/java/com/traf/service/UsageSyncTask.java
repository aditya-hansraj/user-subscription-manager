package com.traf.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.traf.db.SubscriptionDAO;
import io.dropwizard.lifecycle.Managed;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.hibernate.Session;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class UsageSyncTask implements Managed {
    private final SubscriptionDAO subscriptionDAO;
    private final Cache<Long, Integer> usageCache;
    private final ScheduledExecutorService scheduler;
    private final SessionFactory sessionFactory;

    @Inject
    public UsageSyncTask(SubscriptionDAO subscriptionDAO, Cache<Long, Integer> usageCache, SessionFactory sessionFactory) {
        this.subscriptionDAO = subscriptionDAO;
        this.usageCache = usageCache;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void start() {
        scheduler.scheduleAtFixedRate(this::syncToDatabase, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void stop() {
        scheduler.shutdown();
    }

    private void syncToDatabase() {
        Map<Long, Integer> allUsage = usageCache.asMap();
        if (allUsage.isEmpty()) return;

        try (Session session = sessionFactory.openSession()) {
            try {
                ManagedSessionContext.bind(session);
                Transaction transaction = session.beginTransaction();

                allUsage.forEach((userId, count) -> {
                    subscriptionDAO.updateUsage(userId, count);
                });

                transaction.commit();
            } catch (Exception e) {
                if (session.getTransaction().getStatus().canRollback()) {
                    session.getTransaction().rollback();
                }
                e.printStackTrace();
            } finally {
                ManagedSessionContext.unbind(sessionFactory);
            }
        }
    }
 }
