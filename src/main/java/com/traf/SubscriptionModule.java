package com.traf;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.traf.db.PLanDAO;
import com.traf.db.SubscriptionDAO;
import com.traf.db.UserDAO;
import com.traf.repository.UsageRepository;
import com.traf.repository.UserUsageRepository;
import io.dropwizard.hibernate.HibernateBundle;
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
    public Cache<Long, Integer> provideUsageCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(59)) // Cache entries expire after 10 minutes
                .maximumSize(100) // Maximum of 1000 entries in the cache
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
    public PLanDAO providePlanDAO(SessionFactory sessionFactory) {
        return new PLanDAO(sessionFactory);
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
