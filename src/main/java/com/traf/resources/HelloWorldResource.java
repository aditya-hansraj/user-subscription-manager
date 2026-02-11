package com.traf.resources;

import com.traf.core.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("hello")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
    @GET
    public ApiResponse<String> hello() {
        return ApiResponse.success("Hello, World!");
    }
}
