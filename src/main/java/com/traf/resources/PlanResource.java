package com.traf.resources;

import com.traf.core.Plan;
import com.traf.db.PLanDAO;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("plans")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlanResource {

    private final PLanDAO pLanDAO;

    @Inject
    public PlanResource(PLanDAO pLanDAO) {
        this.pLanDAO = pLanDAO;
    }

    @POST
    @UnitOfWork
    public Plan CreatePlan(Plan plan) {
        return pLanDAO.create(plan);
    }

    @GET
    @UnitOfWork
    public List<Plan> getAllPlans() {
        return pLanDAO.findAll();
    }

    @DELETE
    @UnitOfWork
    public long deletePlan(@QueryParam("id") long id) throws Exception {
        boolean res = pLanDAO.deleteById(id);
        return res ? id : 0;
    }
}

