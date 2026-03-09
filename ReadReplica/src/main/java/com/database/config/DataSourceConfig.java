package com.database.config;

import com.database.holder.DbContextHolder;
import com.database.holder.PrimaryReplicaRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "app.datasource.replica")
    public DataSource replicaDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public DataSource routingDataSource() {
        PrimaryReplicaRoutingDataSource routingDataSource = new PrimaryReplicaRoutingDataSource();

        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(DbContextHolder.DbType.PRIMARY, primaryDataSource());
        dataSourceMap.put(DbContextHolder.DbType.REPLICA, replicaDataSource());

        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(primaryDataSource());

        return routingDataSource;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        // Lazy connection prevents Spring from fetching a connection before the transaction starts
        return new LazyConnectionDataSourceProxy(routingDataSource());
    }
}