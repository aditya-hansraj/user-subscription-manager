package com.traf.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("hello")
public class HelloWorldResource {
    @GET
    public String hello() {
        return "Hello, World!";
    }
}
