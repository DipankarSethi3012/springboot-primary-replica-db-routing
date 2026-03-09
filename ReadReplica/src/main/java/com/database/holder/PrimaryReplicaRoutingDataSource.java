package com.database.holder;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class PrimaryReplicaRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return DbContextHolder.getDbType();
    }
}
