package com.database.aop;

import com.database.holder.DbContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Aspect
@Component
public class TransactionRoutingAspect {

    // 1. Initialize the Logger
    private static final Logger logger = LoggerFactory.getLogger(TransactionRoutingAspect.class);

    @Around("@annotation(transactional)")
    public Object routeTransaction(ProceedingJoinPoint joinPoint, Transactional transactional) throws Throwable {
        try {
            if (transactional.readOnly()) {
                DbContextHolder.setDbType(DbContextHolder.DbType.REPLICA);
                // 2. Log for REPLICA
                logger.info("======> Routing database request to: REPLICA (Read-Only)");
            } else {
                DbContextHolder.setDbType(DbContextHolder.DbType.PRIMARY);
                // 3. Log for PRIMARY
                logger.info("======> Routing database request to: PRIMARY (Write)");
            }
            return joinPoint.proceed();
        } finally {
            DbContextHolder.clearDbType();
        }
    }
}