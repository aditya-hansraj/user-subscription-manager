package com.traf.resources;

import com.traf.core.Subscription;
import com.traf.service.SubscriptionService;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("subscriptions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SubscriptionResource {

    private static class SubscriptionRequest {
        @Min(1)
        public long userId;

        @Min(1)
        public long planId;
    }

    private final SubscriptionService subscriptionService;

    @Inject
    public SubscriptionResource(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @POST
    @UnitOfWork
    public Subscription subscribe(@Valid SubscriptionRequest request) throws Exception{

        return subscriptionService.createSubscription(request.userId, request.planId);
    }

    @GET
    @Path("/user/{userId}")
    @UnitOfWork
    public List<Subscription> getSubscriptions(@PathParam("userId") long userId) throws Exception {
        return subscriptionService.getSubscriptions(userId);
    }

    @PUT
    @Path("/{id}/cancel")
    @UnitOfWork
    public Subscription cancel(@PathParam("id") long id) throws Exception {
        return subscriptionService.cancelSubscription(id);
    }

    @PUT
    @Path("/{id}/upgrade")
    @UnitOfWork
    public Subscription upgrade(@PathParam("id") long id, @QueryParam("planId") long planId) throws Exception {
        return subscriptionService.upgradePlan(id, planId);
    }
}