package com.traf.resources;

import com.traf.core.User;
import com.traf.db.UserDAO;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private final UserDAO userDAO;

    @Inject
    public UserResource(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @POST
    @UnitOfWork
    public User createUser(@Valid User user) {
        return userDAO.create(user);
    }

    @GET
    @UnitOfWork
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    @GET
    @Path("/{id}")
    @UnitOfWork
    public User getUser(@PathParam("id") long id) {
        return userDAO.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @DELETE
    @Path("/{id}")
    @UnitOfWork
    public boolean deleteUser(@PathParam("id") long id) {
        return userDAO.deleteById(id);
    }
}