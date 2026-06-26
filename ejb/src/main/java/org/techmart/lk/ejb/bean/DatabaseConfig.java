package org.techmart.lk.ejb.bean;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@DataSourceDefinition(
    name = "java:app/jdbc/TechMartDS",
    className = "com.mysql.cj.jdbc.MysqlDataSource",
    url = "jdbc:mysql://localhost:3307/techmart_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true",
    user = "root",
    password = "dbms@java",

    initialPoolSize = 5,
    minPoolSize = 5,
    maxPoolSize = 50,
    maxIdleTime = 300
)
@Singleton
@Startup
public class DatabaseConfig {

}
