package org.techmart.lk.ejb.bean;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@DataSourceDefinition(
    name = "java:app/jdbc/TechMartDS",
    className = "com.mysql.cj.jdbc.MysqlDataSource",
    url = "jdbc:mysql://localhost:3307/techmart_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true",
    user = "root",
    password = "dbms@java", // NOTE: This hardcoded password is for local development convenience ONLY.
                           // For production environments, credentials are never hardcoded in source files.
                           // Instead, this configuration is externalized entirely using the 
                           // glassfish-resources.xml file (under /deployment/payara/) or system environment
                           // variables injected by the container orchestration platform.
    initialPoolSize = 5,
    minPoolSize = 5,
    maxPoolSize = 50,
    maxIdleTime = 300
)
@Singleton
@Startup
public class DatabaseConfig {
    // This class defines and initializes the application datasource for local dev.
    // In production, the datasource is configured via /deployment/payara/glassfish-resources.xml
}
