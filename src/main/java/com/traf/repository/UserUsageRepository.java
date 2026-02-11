package com.traf.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.traf.core.Subscription;
import com.traf.db.SubscriptionDAO;

import java.util.List;

@Singleton
public class UserUsageRepository implements UsageRepository{
    private final SubscriptionDAO subscriptionDAO;
    private final Cache<Long, Integer> usageCache;

    @Inject
    public UserUsageRepository(SubscriptionDAO subscriptionDAO, Cache<Long, Integer> usageCache) {
        this.subscriptionDAO = subscriptionDAO;
        this.usageCache = usageCache;
    }

    @Override
    public void recordApiHit(long userId) {
        // .get(key, loader) is atomic. If userId isn't there, it runs the loader.
        Integer current = usageCache.get(userId, id -> {
            System.out.println("Cache Miss! Fetching usage for User: " + id);
            return subscriptionDAO.getCurrentUsage(id);
        });

        usageCache.put(userId, current + 1);
    }

    @Override
    public boolean isUnderLimit(long userId) {
        Integer used = usageCache.get(userId, id -> fetchCurrentUsageFromDb(id));

        List<Subscription> subscriptions = subscriptionDAO.findByUserId(userId);
        if(subscriptions.isEmpty()) return false;

        int limit = subscriptionDAO.getPlanLimit(userId);
        return used < limit;
    }

    private int fetchCurrentUsageFromDb(long userId) {
        Integer usage = subscriptionDAO.getCurrentUsage(userId);
        if (usage == null) {
            return 0;
        }
        return usage;
    }

}
