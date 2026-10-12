package org.example.telegrambot.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.example.telegrambot.entity.NotificationTask;
import org.example.telegrambot.entity.User;
import org.example.telegrambot.util.PropertiesLoader;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import jakarta.persistence.EntityManagerFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class JpaConfig {

    private static EntityManagerFactory entityManagerFactory;

    private JpaConfig() {}

    public static EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            synchronized (JpaConfig.class) {
                if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
                    entityManagerFactory = createEntityManagerFactory();
                }
            }
        }
        return entityManagerFactory;
    }

    private static EntityManagerFactory createEntityManagerFactory() {
        String url = PropertiesLoader.getProperty("database.url");
        String username = PropertiesLoader.getProperty("database.username");
        String password = PropertiesLoader.getProperty("database.password");

        // Run Liquibase migrations
        runLiquibase(url, username, password);

        Properties hibernateProps = new Properties();
        hibernateProps.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        hibernateProps.setProperty("hibernate.connection.url", url);
        hibernateProps.setProperty("hibernate.connection.username", username);
        hibernateProps.setProperty("hibernate.connection.password", password);
        hibernateProps.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        hibernateProps.setProperty("hibernate.hbm2ddl.auto", PropertiesLoader.getProperty("hibernate.hbm2ddl.auto"));
        hibernateProps.setProperty("hibernate.show_sql", PropertiesLoader.getProperty("hibernate.show_sql"));
        hibernateProps.setProperty("hibernate.format_sql", PropertiesLoader.getProperty("hibernate.format_sql"));
        hibernateProps.setProperty("hibernate.use_sql_comments", "true");
        hibernateProps.setProperty("hibernate.connection.provider_class",
                "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        hibernateProps.setProperty("hibernate.hikaricp.maximumPoolSize", "5");
        hibernateProps.setProperty("hibernate.hikaricp.minimumIdle", "1");

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(hibernateProps)
                .build();

        entityManagerFactory = new MetadataSources(serviceRegistry)
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(NotificationTask.class)
                .buildMetadata()
                .buildSessionFactory();

        return entityManagerFactory;
    }

    private static void runLiquibase(String url, String username, String password) {
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            JdbcConnection jdbcConnection = new JdbcConnection(conn);
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(jdbcConnection);
            Liquibase liquibase = new Liquibase(
                    "db/changelog/changelog-master.xml",
                    new ClassLoaderResourceAccessor(),
                    database
            );
            liquibase.update((String) null);
            System.out.println("Liquibase migrations completed successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to run Liquibase migrations", e);
        }
    }

    public static void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}
