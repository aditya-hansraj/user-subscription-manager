package com.traf.resources.dto;

import jakarta.validation.constraints.Min;

public class SubscriptionRequest {
    @Min(1)
    private long userId;

    @Min(1)
    private long planId;

    public SubscriptionRequest() {}

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getPlanId() {
        return planId;
    }

    public void setPlanId(long planId) {
        this.planId = planId;
    }
}
