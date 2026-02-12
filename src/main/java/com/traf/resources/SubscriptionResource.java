package com.traf.resources;

import com.traf.core.ApiResponse;
import com.traf.core.Subscription;
import com.traf.resources.dto.SubscriptionRequest;
import com.traf.service.SubscriptionService;
import com.traf.service.UserService;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("subscriptions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SubscriptionResource {

    private final SubscriptionService subscriptionService;
    private final UserService userService;

    @Inject
    public SubscriptionResource(SubscriptionService subscriptionService, UserService userService) {
        this.subscriptionService = subscriptionService;
        this.userService = userService;
    }

    @POST
    @UnitOfWork
    public ApiResponse<Subscription> subscribe(@Valid SubscriptionRequest request) {
        if (request == null) {
            return ApiResponse.fail(400, "Subscription payload is required");
        }
        try {
            if (!userService.exists(request.getUserId())) {
                return ApiResponse.fail(404, "User not found");
            }
            Subscription sub = subscriptionService.createSubscription(request.getUserId(), request.getPlanId());
            return ApiResponse.success(sub);
        } catch (NotFoundException nf) {
            return ApiResponse.fail(404, nf.getMessage());
        } catch (IllegalArgumentException iae) {
            return ApiResponse.fail(400, iae.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(500, "Failed to create subscription: " + e.getMessage());
        }
    }

    @GET
    @Path("/user/{userId}")
    @UnitOfWork
    public ApiResponse<List<Subscription>> getSubscriptions(@PathParam("userId") long userId) {
        if (userId <= 0) {
            return ApiResponse.fail(400, "Valid user id is required");
        }
        try {
            if (!userService.exists(userId)) {
                return ApiResponse.fail(404, "User not found");
            }
            List<Subscription> subs = subscriptionService.getSubscriptions(userId);
            return ApiResponse.success(subs);
        } catch (Exception e) {
            return ApiResponse.fail(500, "Failed to fetch subscriptions: " + e.getMessage());
        }
    }

    @PUT
    @Path("/{id}/cancel")
    @UnitOfWork
    public ApiResponse<Subscription> cancel(@PathParam("id") long id) {
        if (id <= 0) {
            return ApiResponse.fail(400, "Valid subscription id is required");
        }
        try {
            Subscription sub = subscriptionService.cancelSubscription(id);
            return ApiResponse.success(sub);
        } catch (NotFoundException nf) {
            return ApiResponse.fail(404, nf.getMessage());
        } catch (IllegalStateException ise) {
            return ApiResponse.fail(400, ise.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(500, "Failed to cancel subscription: " + e.getMessage());
        }
    }

    @PUT
    @Path("/{id}/upgrade")
    @UnitOfWork
    public ApiResponse<Subscription> upgrade(@PathParam("id") long id, @QueryParam("planId") long planId) {
        if (id <= 0 || planId <= 0) {
            return ApiResponse.fail(400, "Valid subscription id and planId are required");
        }
        try {
            Subscription sub = subscriptionService.upgradePlan(id, planId);
            return ApiResponse.success(sub);
        } catch (NotFoundException nf) {
            return ApiResponse.fail(404, nf.getMessage());
        } catch (IllegalArgumentException iae) {
            return ApiResponse.fail(400, iae.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(500, "Failed to upgrade subscription: " + e.getMessage());
        }
    }
}