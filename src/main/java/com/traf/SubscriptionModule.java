package com.traf;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.traf.db.PlanDAO;
import com.traf.db.SubscriptionDAO;
import com.traf.db.UserDAO;
import com.traf.repository.UsageRepository;
import com.traf.repository.UserUsageRepository;
import com.traf.service.UsageSyncTask;
import io.dropwizard.hibernate.HibernateBundle;
import jakarta.inject.Provider;
import org.hibernate.SessionFactory;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;

public class SubscriptionModule extends AbstractModule {

    private final HibernateBundle<AppConfiguration> hibernateBundle;

    public SubscriptionModule(HibernateBundle<AppConfiguration> hibernateBundle) {
        this.hibernateBundle = hibernateBundle;
    }

    @Provides
    @Singleton
    public Cache<Long, Integer> provideUsageCache(Provider<UsageSyncTask> syncTaskProvider) {
        return Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofMinutes(59))
                .evictionListener((Long userId, Integer count, RemovalCause cause) -> {
                    if (cause.wasEvicted()) {
                        // Fetch the actual task instance only when needed
                        UsageSyncTask task = syncTaskProvider.get();
                        task.syncToDatabaseForUser(userId, count);
                    }
                })
                .build();
    }

    @Override
    protected void configure() {
        bind(UsageRepository.class).to(UserUsageRepository.class);
    }

    @Provides
    @Singleton
    public SessionFactory provideSessionFactory() {
        // This bridges the Dropwizard-managed factory into the Guice world
        return hibernateBundle.getSessionFactory();
    }

    @Provides
    @Singleton
    public PlanDAO providePlanDAO(SessionFactory sessionFactory) {
        return new PlanDAO(sessionFactory);
    }

    @Provides
    @Singleton
    public UserDAO provideUserDAO(SessionFactory sessionFactory) {
        return new UserDAO(sessionFactory);
    }

    @Provides
    @Singleton
    public SubscriptionDAO provideSubscriptionDAO(SessionFactory sessionFactory) {
        return new SubscriptionDAO(sessionFactory);
    }

}
