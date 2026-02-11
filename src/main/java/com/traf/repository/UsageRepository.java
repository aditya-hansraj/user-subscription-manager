package com.traf.repository;

public interface UsageRepository {
    void recordApiHit(long userId);
    boolean isUnderLimit(long userId);
}
