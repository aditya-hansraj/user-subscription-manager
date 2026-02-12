package com.traf.resources;

import com.google.inject.Inject;
import com.traf.core.ApiResponse;
import com.traf.repository.UsageRepository;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("api")
@Produces(MediaType.APPLICATION_JSON)
public class RateLimiterResource {

    private final UsageRepository usageRepository;

    @Inject
    public RateLimiterResource(UsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    @GET
    @Path("/ping")
    @UnitOfWork
    public ApiResponse<String> ping(@QueryParam("userId") long userId) {
        if (userId <= 0) {
            return ApiResponse.fail(400, "User ID is not valid");
        }
        try {
            if (!usageRepository.isUnderLimit(userId)) {
                return ApiResponse.fail(429, "Rate limit exceeded. Upgrade your plan!");
            }

            usageRepository.recordApiHit(userId);
            return ApiResponse.success("Request successful.");
        } catch (Exception e) {
            return ApiResponse.fail(500, "Failed to process request: " + e.getMessage());
        }
    }
}
