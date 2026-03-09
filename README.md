# Spring Boot Primary--Replica Database Routing

## Overview

This project demonstrates **dynamic database routing in Spring Boot**
using a **Primary--Replica database architecture**.

The system automatically routes:

-   **Write operations → Primary Database**
-   **Read operations → Replica Database**

Routing is implemented using **Spring AOP**, **ThreadLocal context**,
and **Spring's `AbstractRoutingDataSource`**.

This architecture is commonly used in **high-traffic production
systems** to improve performance and scalability.

------------------------------------------------------------------------

# Architecture

    Client Request
          │
          ▼
    Service Layer (@Transactional)
          │
          ▼
    AOP Aspect (TransactionRoutingAspect)
          │
          ▼
    DbContextHolder (ThreadLocal)
          │
          ▼
    Routing DataSource
          │
     ┌───────┴────────┐
     ▼                ▼
    Primary DB      Replica DB
    (write)         (read)

------------------------------------------------------------------------

# Key Components

## 1. TransactionRoutingAspect (AOP Layer)

This aspect intercepts methods annotated with `@Transactional` and
determines whether the transaction is **read-only** or **write**.

  Transaction Type                    Database Selected
  ----------------------------------- -------------------
  `@Transactional(readOnly = true)`   Replica
  `@Transactional`                    Primary

Example:

``` java
@Around("@annotation(transactional)")
public Object routeTransaction(ProceedingJoinPoint joinPoint, Transactional transactional) throws Throwable {
    try {
        if (transactional.readOnly()) {
            DbContextHolder.setDbType(DbContextHolder.DbType.REPLICA);
        } else {
            DbContextHolder.setDbType(DbContextHolder.DbType.PRIMARY);
        }
        return joinPoint.proceed();
    } finally {
        DbContextHolder.clearDbType();
    }
}
```

------------------------------------------------------------------------

## 2. DbContextHolder

Stores the **current database type** using `ThreadLocal` so each request
maintains its own routing context.

Example:

    Thread A → REPLICA
    Thread B → PRIMARY
    Thread C → REPLICA

------------------------------------------------------------------------

## 3. PrimaryReplicaRoutingDataSource

Extends Spring's `AbstractRoutingDataSource` and determines which
database should be used.

``` java
public class PrimaryReplicaRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return DbContextHolder.getDbType();
    }
}
```

Before executing a query, Spring calls:

    determineCurrentLookupKey()

It returns either:

    PRIMARY
    or
    REPLICA

------------------------------------------------------------------------

## 4. DataSource Configuration

Defines:

-   Primary DataSource
-   Replica DataSource
-   Routing DataSource
-   Lazy connection proxy

Example:

``` java
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
```

------------------------------------------------------------------------

## 5. Routing DataSource

    PRIMARY  → Primary Database
    REPLICA  → Replica Database

Example:

``` java
Map<Object, Object> dataSourceMap = new HashMap<>();
dataSourceMap.put(DbContextHolder.DbType.PRIMARY, primaryDataSource());
dataSourceMap.put(DbContextHolder.DbType.REPLICA, replicaDataSource());
```

Default datasource is **Primary**.

------------------------------------------------------------------------

## 6. LazyConnectionDataSourceProxy

Ensures a database connection is **not fetched until actually
required**.

This prevents incorrect database routing if Spring attempts to obtain a
connection before the transaction logic runs.

------------------------------------------------------------------------

# Configuration Example (application.properties)

    app.datasource.primary.url=jdbc:mysql://primary-db:3306/app
    app.datasource.primary.username=root
    app.datasource.primary.password=password

    app.datasource.replica.url=jdbc:mysql://replica-db:3306/app
    app.datasource.replica.username=root
    app.datasource.replica.password=password

------------------------------------------------------------------------

# Example Usage

## Read Query

``` java
@Transactional(readOnly = true)
public List<User> getUsers() {
    return userRepository.findAll();
}
```

Result:

    Query executed on REPLICA database

------------------------------------------------------------------------

## Write Query

``` java
@Transactional
public void createUser(User user) {
    userRepository.save(user);
}
```

Result:

    Query executed on PRIMARY database

------------------------------------------------------------------------

# Benefits

-   Improves database scalability
-   Reduces load on primary database
-   Enables horizontal scaling
-   Separates read and write workloads
-   Improves application performance

------------------------------------------------------------------------

# Technologies Used

-   Spring Boot
-   Spring AOP
-   Spring JDBC
-   HikariCP
-   ThreadLocal
-   AbstractRoutingDataSource

------------------------------------------------------------------------

# Summary

This project implements **dynamic database routing** using:

1.  Spring AOP to detect transaction type
2.  ThreadLocal context to store database selection
3.  AbstractRoutingDataSource for dynamic datasource routing
4.  LazyConnectionDataSourceProxy to delay connection acquisition

This enables a **production-ready primary--replica database
architecture** that improves scalability and performance.
