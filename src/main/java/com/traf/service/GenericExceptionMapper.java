package com.traf.service;

import com.traf.core.ApiResponse;
import com.traf.exceptions.BadRequestException;
import com.traf.exceptions.ConflictException;
import com.traf.exceptions.NotFoundException;
import com.traf.exceptions.RateLimitExceededException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger log = LoggerFactory.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        log.error("Unhandled exception", exception);

        ApiResponse<?> body;
        int status;

        if (exception instanceof BadRequestException) {
            status = 400;
            body = ApiResponse.fail(status, exception.getMessage());
        } else if (exception instanceof NotFoundException) {
            status = 404;
            body = ApiResponse.fail(status, exception.getMessage());
        } else if (exception instanceof ConflictException) {
            status = 409;
            body = ApiResponse.fail(status, exception.getMessage());
        } else if (exception instanceof RateLimitExceededException) {
            status = 429;
            body = ApiResponse.fail(status, exception.getMessage());
        } else {
            status = 500;
            body = ApiResponse.fail(status, "An unexpected error occurred: " + exception.getMessage());
        }

        return Response.status(status)
                .entity(body)
                .build();
    }
}
