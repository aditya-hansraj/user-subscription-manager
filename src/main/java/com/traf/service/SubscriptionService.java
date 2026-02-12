package com.traf.service;

import com.traf.core.Plan;
import com.traf.core.Subscription;
import com.traf.core.User;
import com.traf.db.PlanDAO;
import com.traf.db.SubscriptionDAO;
import com.traf.db.UserDAO;
import jakarta.inject.Inject;
import javassist.NotFoundException;

import java.util.List;

public class SubscriptionService {

    private final PlanDAO planDAO;
    private final UserDAO userDAO;
    private final SubscriptionDAO subscriptionDAO;

    @Inject
    public SubscriptionService(PlanDAO pLanDAO, UserDAO userDAO, SubscriptionDAO subscriptionDAO) {
        this.planDAO = pLanDAO;
        this.userDAO = userDAO;
        this.subscriptionDAO = subscriptionDAO;
    }

    public Subscription createSubscription(long userId, long planId) throws Exception {
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        Plan plan = planDAO.findById(planId)
                .orElseThrow(() -> new NotFoundException("Plan with ID " + planId + " not found"));

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStatus("ACTIVE");

        return subscriptionDAO.create(subscription);
    }

    public Subscription getSubscriptionById(long subscriptionId) throws Exception {
        return subscriptionDAO.findById(subscriptionId)
                .orElseThrow(() -> new NotFoundException("Subscription not found with id: " + subscriptionId));
    }

    public List<Subscription> getSubscriptions(long userId) throws Exception {
        return subscriptionDAO.findByUserId(userId);
    }

    public Subscription cancelSubscription(long subscriptionId) throws Exception {
        Subscription sub = subscriptionDAO.findById(subscriptionId)
                .orElseThrow(() -> new NotFoundException("Subscription not found"));

        sub.setStatus("CANCELLED");
        return subscriptionDAO.create(sub);
    }

    public Subscription upgradePlan(long subscriptionId, long newPlanId) throws Exception {
        Subscription sub = subscriptionDAO.findById(subscriptionId)
                .orElseThrow(() -> new NotFoundException("Subscription not found"));

        Plan newPlan = planDAO.findById(newPlanId)
                .orElseThrow(() -> new NotFoundException("New Plan not found"));

        sub.setPlan(newPlan);
        return subscriptionDAO.create(sub);
    }
}
