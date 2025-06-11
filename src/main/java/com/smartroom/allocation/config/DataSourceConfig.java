package com.smartroom.allocation.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class DataSourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);
    private final HikariDataSource dataSource;

    @Autowired
    public DataSourceConfig(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PreDestroy
    public void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Closing HikariDataSource to release all connections");
            dataSource.close();
            logger.info("HikariDataSource successfully closed");
        } else {
            logger.warn("HikariDataSource is null or already closed");
        }
    }
}