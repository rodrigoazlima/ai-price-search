package dev.rodrigoazlima.poc.ai.pricenotifier.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(DatabaseConfig.class, DatabaseProperties.class)
            .withConfiguration(AutoConfigurations.of(
                    DataSourceAutoConfiguration.class,
                    JdbcTemplateAutoConfiguration.class,
                    TransactionAutoConfiguration.class,
                    HealthContributorAutoConfiguration.class,
                    HealthEndpointAutoConfiguration.class
            ))
            .withPropertyValues(
                    "spring.datasource.driver-class-name=org.h2.Driver",
                    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
                    "spring.datasource.username=sa",
                    "spring.datasource.password="
            );

    @Test
    void contextLoads() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(DataSource.class);
            assertThat(context).hasSingleBean(JdbcTemplate.class);
        });
    }

    @Test
    void testDataSourceConnection() {
        contextRunner.run(context -> {
            DataSource dataSource = context.getBean(DataSource.class);
            assertNotNull(dataSource, "DataSource should not be null");
            try (Connection connection = dataSource.getConnection()) {
                assertNotNull(connection, "Connection should not be null");
                assertFalse(connection.isClosed(), "Connection should be open");
                assertTrue(connection.isValid(1), "Connection should be valid");
            }
        });
    }

    @Test
    void testDatabaseConnectivity() {
        contextRunner.run(context -> {
            JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            assertEquals(1, result, "Database query should return 1");
        });
    }

    @Test
    void testDataSourceProperties() {
        contextRunner.run(context -> {
            DataSource dataSource = context.getBean(DataSource.class);
            try (Connection connection = dataSource.getConnection()) {
                String databaseProductName = connection.getMetaData().getDatabaseProductName();
                assertNotNull(databaseProductName, "Database product name should not be null");
            }
        });
    }

    @Test
    void testJdbcTemplateQueryExecution() {
        contextRunner.run(context -> {
            JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES",
                    Integer.class
            );
            assertNotNull(count, "Table count should not be null");
            assertTrue(count >= 0, "Table count should be non-negative");
        });
    }

    @Test
    void testTransactionSupport() {
        contextRunner.run(context -> {
            JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
            assertDoesNotThrow(() -> jdbcTemplate.execute("SELECT 1"),
                    "Transaction should execute without errors");
        });
    }

    @Test
    void testConnectionPooling() {
        contextRunner.run(context -> {
            DataSource dataSource = context.getBean(DataSource.class);
            try (Connection conn1 = dataSource.getConnection();
                 Connection conn2 = dataSource.getConnection()) {
                assertNotNull(conn1);
                assertNotNull(conn2);
                assertNotSame(conn1, conn2, "Connections should be different instances");
            }
        });
    }
}