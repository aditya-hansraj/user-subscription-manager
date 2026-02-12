package com.traf.resources;

import com.traf.core.ApiResponse;
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
    public ApiResponse<User> createUser(@Valid User user) {
        if (user == null) {
            return ApiResponse.fail(400, "User is null");
        }
        try {
            User created = userDAO.create(user);
            return ApiResponse.success(created);
        } catch (Exception e) {
            return ApiResponse.fail(500, "Failed to create user: " + e.getMessage());
        }
    }


    @GET
    @UnitOfWork
    public ApiResponse<List<User>> getAllUsers() {
        try {
            List<User> users = userDAO.findAll();
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.fail(500, "Failed to fetch users: " + e.getMessage());
        }
    }

    @GET
    @Path("/{id}")
    @UnitOfWork
    public ApiResponse<User> getUser(@PathParam("id") long id) {
        if (id <= 0) {
            return ApiResponse.fail(400, "Invalid user id");
        }
        try {
            return userDAO.findById(id)
                    .map(ApiResponse::success)
                    .orElseGet(() -> ApiResponse.fail(404, "User not found"));
        } catch (Exception e) {
            return ApiResponse.fail(500, "Failed to fetch user: " + e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    @UnitOfWork
    public ApiResponse<Long> deleteUser(@PathParam("id") long id) {
        if (id <= 0) {
            return ApiResponse.fail(400, "Invalid user id");
        }
        try {
            boolean deleted = userDAO.deleteById(id);
            if (deleted) {
                return ApiResponse.success(id);
            }
            return ApiResponse.fail(404, "User not found");
        } catch (Exception e) {
            return ApiResponse.fail(500, "Failed to delete user: " + e.getMessage());
        }
    }
}