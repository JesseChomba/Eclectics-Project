package com.smartroom.allocation.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PreDestroy;

@Configuration
public class DataSourceConfig {
    private final HikariDataSource dataSource;

    @Autowired
    public DataSourceConfig(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PreDestroy
    public void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("HikariDataSource closed on application shutdown");
        }
    }
}