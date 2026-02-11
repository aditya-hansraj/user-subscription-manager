package com.traf;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;
import io.dropwizard.db.DataSourceFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class AppConfiguration extends Configuration {
    @Valid
    @NotNull
    private DataSourceFactory db = new DataSourceFactory();

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return db;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory db) {
        this.db = db;
    }
}
