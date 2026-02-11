package com.traf.resources;

import com.google.inject.Inject;
import com.traf.repository.UsageRepository;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
    public Response ping(@QueryParam("userId") long userId) {
        if (!usageRepository.isUnderLimit(userId)) {
            return Response.status(429)
                    .entity("{\"error\": \"Rate limit exceeded. Upgrade your plan!\"}")
                    .build();
        }

        usageRepository.recordApiHit(userId);

        return Response.ok("{\"message\": \"Request successful.\"}")
                .build();
    }
}
