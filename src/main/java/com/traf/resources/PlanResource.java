package com.traf.resources;

import com.traf.core.ApiResponse;
import com.traf.core.Plan;
import com.traf.db.PlanDAO;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("plans")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlanResource {

    private final PlanDAO pLanDAO;

    @Inject
    public PlanResource(PlanDAO pLanDAO) {
        this.pLanDAO = pLanDAO;
    }

    @POST
    @UnitOfWork
    public ApiResponse<Plan> CreatePlan(Plan plan) {
        if (plan == null) {
            return ApiResponse.fail(400, "Plan is null");
        }
        try {
            Plan created = pLanDAO.create(plan);
            return ApiResponse.success(created);
        } catch (Exception e) {
            return ApiResponse.fail(500, "Failed to create plan: " + e.getMessage());
        }
    }

    @GET
    @UnitOfWork
    public ApiResponse<List<Plan>> getAllPlans() {
        try {
            List<Plan> plans = pLanDAO.findAll();
            return ApiResponse.success(plans);
        } catch (Exception e) {
            return ApiResponse.fail(500, "Failed to fetch plans: " + e.getMessage());
        }
    }

    @DELETE
    @UnitOfWork
    public ApiResponse<Long> deletePlan(@QueryParam("id") long id) {
        if (id <= 0) {
            return ApiResponse.fail(400, "Valid plan id is required");
        }
        try {
            boolean deleted = pLanDAO.deleteById(id);
            if (deleted) {
                return ApiResponse.success(id);
            }
            return ApiResponse.fail(404, "Plan not found for id: " + id);
        } catch (Exception e) {
            return ApiResponse.fail(500, "Failed to delete plan: " + e.getMessage());
        }
    }
}
